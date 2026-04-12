# СДК BarcodeService

## Введение

BarcodeService - системное приложение предназначенное для интеграции со сканером ТСД,
которое предоставляет приложениям единый интерфейс для взаимодействия с различными
сканерами. Для управления. Основные функции - управление настройками сканера и получение
информации о сканировании. Для взаимодействия с приложениями разработаны aidl-интерфейс и
библиотека SDK.

Поддерживаемые устройства и сканеры:

- Atol Smart.Lite (сканер Zebra SE4710)
- Atol Smart.Slim (сканеры E3 и Zebra SE4710)
- Atol Smart.Slim+ (сканеры E3 и Zebra SE4710)
- Atol Smart.Pro (сканер Zebra SE4750)
- Atol Smart.Prime (сканеры E3 и Zebra SE4100)

## Подключение

Чтобы подключить SDK, нужно добавить библиотеку в зависимости проекта Android Studio.
Например, поместить файл .aar в /app/libs или указать напрямую зависимость в gradle.
После подключения зависимости становится доступно пространство имен
ru.atol.barcodeservice.api
В нем есть два основных класса:

- ScannerSettings - для управления настройками;
- BarcodeReceiver - для получения сканирования.

## Использование

### Чтение и запись настроек

Для управления настроек необходимо создать экземпляр класса
ru.atol.barcodeservice.api.ScannerSettings, в экземпляре класса вызвать метод bindService() и
подписаться на событие onServiceConnected(). После срабатывания этого события можно читать и
записывать настройки. По окончанию работы с настройками нужно вызвать unbindService().

#### Пример управления настройками

Пример управления настройками через GUI из Fragment()

```
import ru.atol.barcodeservice.api.ScannerSettings

class SettingsDocFragment : Fragment() {
    private val settings = object: ScannerSettings() {
        override fun onServiceConnected() {
            // Читать настройки
            readSettings()
        }
    }

    override fun onResume() {
        super.onResume()
        // Подключиться к сервису управления настройками barcodeService
        settings.bindService(requireContext())
    }

    override fun onPause() {
        super.onPause()
        // Отключиться от сервиса
        settings.unbindService(requireContext())
    }

    // Чтение настроек в GUI и обработчик события изменений в GUI для записи значений
    private fun readSettings() {
        try {
            light.isChecked = settings.lightFlashEnable
            light.setOnClickListener { settings.lightFlashEnable = light.isChecked }
            aim.isChecked = settings.lightAimEnable
            aim.setOnClickListener { settings.lightAimEnable = aim.isChecked }
            ean13.isChecked = settings.codes.ean13.enable
            ean13.setOnClickListener { settings.codes.ean13.enable = ean13.isChecked }
            datamatrix.isChecked = settings.codes.datamatrix.enable
            datamatrix.setOnClickListener { settings.codes.datamatrix.enable = datamatrix.isChecked }
            qr.isChecked = settings.codes.qrcode.enable
            qr.setOnClickListener { settings.codes.qrcode.enable = qr.isChecked }
        } catch (e:Exception) {
            showError("Ошибка чтения настроек: ${e.localizedMessage}")
        }
    }
    <…>
}
```

#### Профили настроек

Настройки сканера привязаны к профилям, выбрать профиль и привязать приложение к профилю
настроек сканирования можно с помощью методов:

```
// Выбрать активный профиль (если такого нет, он будет создан)
settings.setProfile(profileName)

// Привязать приложение к профилю сканирования
settings.setPackage(packageName, profileName)
```

#### Управление настройками через нумерованные параметры

Так же все настройки можно читать и записывать с помощью универсальных методов:

```
// Прочитать параметр
val value = settings.getParameterValue(num)

// Записать параметр
settings.setParameterValue(num, value)
```

### Полный список настроек по номерам

| №  | Наименование         | Тип      | Описание                           |
|----|--------------------|----------|------------------------------------|
| 1  | timeOut            | Int      | Максимальное время сканирования    |
| 2  | lightFlashEnable   | Boolean  | Включать подсветку при сканировании |
| 3  | lightFlashTwinkle  | Boolean  | Включить мерцающую подсветку         |
| 4  | lightAimEnable    | Boolean  | Включать прицел при сканировании   |
| 5  | barcodeSendMode   | Int      | Отправка сканирования через         |
| 6  | barcodeSendDelay | Int      | Задержка нажатия клавиш           |
| 7  | viewSize         | Int      | Размер поля сканирования           |
| 8  | workMode        | Int      | Режим сканирования                  |
| 9  | inverse         | Boolean  | Включить распознавание инверсных кодов |
| 10 | successNotification | Int   | Уведомление об успешном сканировании |
| 11 | multiscanCount   | Int      | Максимальная длина серии (для серийных режимов сканирования) |
| 12 | barcodeSendIntent | String  | Intent Name                       |
| 13 | barcodeSendData  | String   | Extra Data                        |
| 14 | barcodeSendSymbology | String | Extra Symbology                |
| 20 | aimCodeEnable    | Boolean  | Включить передачу AIM ID          |
| 21 | enableLeftScankey | Boolean | Сканировать по нажатию левой кнопки |
| 22 | enableRightScankey | Boolean | Сканировать по нажатию правой кнопки |
| 23 | enableFrontScankey | Boolean | Сканировать по нажатию центральной кнопки |
| 31 | preKeystroke1    | Int      | Клавиша1 до сканирования         |
| 32 | preKeystroke2    | Int      | Клавиша2 до сканирования         |
| 33 | postKeystroke1   | Int      | Клавиша1 после сканирования    |
| 34 | postKeystroke2   | Int      | Клавиша2 после сканирования    |
| 35 | keystrokeSendDelay | Int      | Задержка вывода шк                |
| 41 | prefixChar1     | String   | Префикс1                        |
| 42 | prefixChar2     | String   | Префикс2                        |
| 43 | suffixChar1     | String   | Суффикс1                        |
| 44 | suffixChar2     | String   | Суффикс2                        |
| 51 | lettersCase   | Int      | Преобразование регистра          |
| 52 | modifyGS       | String   | NO_CHAR                       |
| 102| profileAutoChange | Boolean | Автоматическая смена профиля по активному приложению |

#### Настройки штрих-кодов

##### Aztec

| №   | Наименование       | Тип     |
|-----|------------------|---------|
| 1100| azteccodeEnable  | Boolean |
| 1101| azteccodeInverse | Boolean |
| 1190| azteccodeMax    | Int     |
| 1191| azteccodeMin    | Int     |

##### Code-11

| №   | Наименование              | Тип     |
|-----|------------------------|---------|
| 1200| code11Enable           | Boolean |
| 1201| code11CheckDigit     | Boolean |
| 1202| code11CheckDigitMode | Int     |
| 1203| code11CheckDigitTransmit | Boolean |
| 1290| code11Max           | Int     |
| 1291| code11Min           | Int     |

##### Code-39

| №   | Наименование              | Тип     |
|-----|------------------------|---------|
| 1300| code39Enable           | Boolean |
| 1301| code39StartStopTransmit | Boolean |
| 1302| code39Append          | Boolean |
| 1303| code39CheckDigit    | Boolean |
| 1304| code39CheckDigitTransmit | Boolean |
| 1305| code39FullAsci      | Boolean |
| 1306| code39Base32        | Boolean |
| 1307| code39Trioptic     | Boolean |
| 1308| code39TcifLinked   | Boolean |
| 1390| code39Max           | Int     |
| 1391| code39Min           | Int     |

##### Code-93

| №   | Наименование | Тип     |
|-----|-------------|---------|
| 1400| code93Enable | Boolean |
| 1490| code93Max   | Int     |
| 1491| code93Min   | Int     |

##### Code-128

| №   | Наименование         | Тип     |
|-----|------------------|---------|
| 1500| code128Enable    | Boolean |
| 1501| code128Inverse  | Boolean |
| 1502| code128Isbt128 | Boolean |
| 1590| code128Max     | Int     |
| 1591| code128Min     | Int     |

##### Codabar

| №   | Наименование               | Тип     |
|-----|------------------------|---------|
| 1600| codabarEnable         | Boolean |
| 1601| codabarStartStop    | Boolean |
| 1602| codabarCheckDigit | Boolean |
| 1603| codabarCheckDigitTransmit | Boolean |
| 1604| codabarClsi      | Boolean |
| 1690| codabarMax       | Int     |
| 1691| codabarMin       | Int     |

##### Codablock-F

| №   | Наименование    | Тип     |
|-----|--------------|---------|
| 1700| codablockfEnable | Boolean |
| 1790| codablockfMax | Int     |
| 1791| codablockfMin | Int     |

##### Data Matrix

| №   | Наименование                | Тип     |
|-----|--------------------------|---------|
| 1800| datamatrixEnable       | Boolean |
| 1801| datamatrixReverse      | Boolean |
| 1802| datamatrixMirror      | Boolean |
| 1803| gs1datamatrixEnable   | Boolean |
| 1804| gs1datamatrixSendFncPrefix | Boolean |
| 1890| datamatrixMax        | Int     |
| 1891| datamatrixMin        | Int     |

##### EAN-8

| №   | Наименование             | Тип     |
|-----|------------------------|---------|
| 1900| ean8Enable            | Boolean |
| 1901| ean8TransmitCheck    | Boolean |
| 1902| ean82charAddenda   | Boolean |
| 1903| ean85charAddenda   | Boolean |
| 1904| ean8RequiredAddenda | Boolean |
| 1905| ean8SeparatorAddenda | Boolean |
| 1906| ean8Extend         | Boolean |

##### EAN-13

| №   | Наименование                | Тип     |
|-----|---------------------------|---------|
| 2000| ean13Enable              | Boolean |
| 2001| ean13TransmitCheck      | Boolean |
| 2002| ean132charAddenda    | Boolean |
| 2003| ean135charAddenda | Boolean |
| 2004| ean13RequiredAddenda | Boolean |
| 2005| ean13SeparatorAddenda | Boolean |
| 2006| ean13IsbnEnable    | Boolean |

##### GS1 DataBar

| №   | Наименование                | Тип     |
|-----|---------------------------|---------|
| 2100| gs1databarEnable        | Boolean |
| 2101| gs1databarExpanded    | Boolean |
| 2102| gs1databarLimited     | Boolean |
| 2103| gs1databarLimitedSecurity | Int |
| 2104| gs1databarOmnidirectional | Boolean |
| 2190| gs1databarExpandedMax | Int |
| 2191| gs1databarExpandedMin | Int |

##### Han Xin

| №   | Наименование   | Тип     |
|-----|--------------|---------|
| 2200| hanxinEnable | Boolean |
| 2201| hanxinInverse | Boolean |
| 2290| hanxinMax   | Int     |
| 2291| hanxinMin   | Int     |

##### Datalogic 2 of 5

| №   | Наименование | Тип     |
|-----|-------------|---------|
| 2300| hk25Enable | Boolean |
| 2390| hk25Max   | Int     |
| 2391| hk25Min   | Int     |

##### Interleaved 2 of 5

| №   | Наименование                    | Тип     |
|-----|-------------------------------|---------|
| 2400| interleaved25Enable          | Boolean |
| 2401| interleaved25StartStopTransmit | Boolean |
| 2402| interleaved25CheckDigitMode  | Int     |
| 2403| interleaved25CheckDigitTransmit | Boolean |
| 2490| interleaved25Max            | Int     |
| 2491| interleaved25Min            | Int     |

##### Matrix 2 of 5

| №   | Наименование               | Тип     |
|-----|-------------------------|---------|
| 2500| matrix25Enable          | Boolean |
| 2501| matrix25CheckDigit    | Boolean |
| 2502| matrix25CheckDigitTransmit | Boolean |
| 2590| matrix25Max            | Int     |
| 2591| matrix25Min            | Int     |

##### MaxiCode

| №   | Наименование | Тип     |
|-----|-------------|---------|
| 2600| maxicodeEnable | Boolean |
| 2690| maxicodeMax   | Int     |
| 2691| maxicodeMin  | Int     |

##### Micro PDF417

| №   | Наименование    | Тип     |
|-----|-----------------|---------|
| 2700| micropdfEnable | Boolean |
| 2790| micropdfMax   | Int     |
| 2791| micropdfMin   | Int     |

##### PDF417

| №   | Наименование | Тип     |
|-----|-------------|---------|
| 2800| pdf417Enable | Boolean |
| 2890| pdf417Max   | Int     |
| 2891| pdf417Min   | Int     |

##### QR Code и Micro QR

| №   | Наименование      | Тип     |
|-----|-------------------|---------|
| 2900| qrcodeEnable     | Boolean |
| 2901| qrcodeReverse   | Boolean |
| 2902| qrcodeMqr       | Boolean |
| 2903| qrcodeMqrReverse | Boolean |
| 2904| qrcodeMirror    | Boolean |
| 2990| qrcodeMax       | Int     |
| 2991| qrcodeMin       | Int     |

##### Telepen

| №   | Наименование | Тип     |
|-----|-------------|---------|
| 3000| telepenEnable | Boolean |
| 3090| telepenMax   | Int     |
| 3091| telepenMin   | Int     |

##### UPC-A

| №   | Наименование               | Тип     |
|-----|--------------------------|---------|
| 3100| upcaEnable             | Boolean |
| 3101| upcaTransmitCheck      | Boolean |
| 3102| upcaNumberSystem      | Boolean |
| 3103| upca2charAddenda      | Boolean |
| 3104| upca5charAddenda      | Boolean |
| 3105| upcaRequiredAddenda   | Boolean |
| 3106| upcaSeparatorAddenda | Boolean |
| 3107| upcaCountryCode      | Boolean |

##### UPC-E

| №   | Наименование               | Тип     |
|-----|--------------------------|---------|
| 3200| upceEnable             | Boolean |
| 3201| upceExpand            | Boolean |
| 3202| upceTransmitCheck      | Boolean |
| 3203| upceNumberSystem      | Boolean |
| 3204| upce2charAddenda      | Boolean |
| 3205| upce5charAddenda      | Boolean |
| 3206| upceRequiredAddenda   | Boolean |
| 3207| upceSeparatorAddenda | Boolean |
| 3208| upceCountryCode      | Boolean |
| 3209| upceConvertToUpca    | Boolean |

##### EAN/UPC дополнения

| №   | Наименование                | Тип     |
|-----|-----------------------------|---------|
| 3210| eanupcDecodeSupplementals  | Boolean |
| 3211| eanupcRequireSupplementals | Boolean |

##### GS1-128

| №   | Наименование | Тип     |
|-----|-------------|---------|
| 3300| gs1128Enable | Boolean |
| 3390| gs1128Max   | Int     |
| 3391| gs1128Min   | Int     |

##### China Post

| №   | Наименование   | Тип     |
|-----|---------------|---------|
| 3400| chinapostEnable | Boolean |
| 3490| chinapostMax   | Int     |
| 3491| chinapostMin   | Int     |

##### Codablock-A

| №   | Наименование     | Тип     |
|-----|-----------------|---------|
| 3500| codablockaEnable | Boolean |
| 3590| codablockaMax   | Int     |
| 3591| codablockaMin   | Int     |

##### GS1 Composite Codes

| №   | Наименование               | Тип     |
|-----|-------------------------|---------|
| 3600| gs1compositeEnable     | Boolean |
| 3601| gs1compositeUpcEan  | Boolean |
| 3690| gs1compositeMax   | Int     |
| 3691| gs1compositeMin   | Int     |

##### Korea Post

| №   | Наименование             | Тип     |
|-----|------------------------|---------|
| 3700| koreapostEnable       | Boolean |
| 3701| koreapostCheckDigit | Boolean |
| 3790| koreapostMax        | Int     |
| 3791| koreapostMin        | Int     |

##### MSI

| №   | Наименование             | Тип     |
|-----|------------------------|---------|
| 3800| msiEnable             | Boolean |
| 3801| msiCheckDigitMode   | Int     |
| 3802| msiCheckDigitTransmit | Boolean |
| 3891| msiMax            | Int     |
| 3892| msiMin            | Int     |

##### MSI-Plessey

| №   | Наименование                | Тип     |
|-----|--------------------------|---------|
| 3900| msiPlesseyEnable        | Boolean |
| 3901| msiPlesseyCheckDigitMode | Int     |
| 3902| msiPlesseyCheckDigitTransmit | Boolean |
| 3990| msiPlesseyMax        | Int     |
| 3991| msiPlesseyMin        | Int     |

##### Nec 2 of 5

| №   | Наименование               | Тип     |
|-----|-------------------------|---------|
| 4000| nec25Enable             | Boolean |
| 4001| nec25StartStopTransmit   | Boolean |
| 4090| nec25Max               | Int     |
| 4091| nec25Min               | Int     |

##### Straight 2 of 5

| №   | Наименование | Тип     |
|-----|-------------|---------|
| 4100| straight25Enable | Boolean |
| 4190| straight25Max   | Int     |
| 4191| straight25Min   | Int     |

##### Straight 2 of 5 IATA

| №   | Наименование       | Тип     |
|-----|------------------|---------|
| 4200| straight25IataEnable | Boolean |
| 4290| straight25IataMax   | Int     |
| 4291| straight25IataMin   | Int     |

##### Discrete 2 of 5

| №   | Наименование   | Тип     |
|-----|-------------|---------|
| 4300| discrete25Enable | Boolean |
| 4390| discrete25Max   | Int     |
| 4391| discrete25Min   | Int     |

##### IATA 2 of 5

| №   | Наименование | Тип     |
|-----|-------------|---------|
| 4400| iata25Enable | Boolean |
| 4490| iata25Max   | Int     |
| 4491| iata25Min   | Int     |

##### Composite CCC

| №   | Наименование              | Тип     |
|-----|-------------------------|---------|
| 4510| compositeCCCEnable    | Boolean |
| 4520| compositeCCABEnable  | Boolean |
| 4530| compositeTLC39Enable | Boolean |
| 4540| compositeUPCMode    | Int     |
| 4550| compositeUCCEANGS1128Emulation | Boolean |

##### UPC-E1

| №   | Наименование            | Тип     |
|-----|----------------------|---------|
| 4600| upce1Enable         | Boolean |
| 4602| upce1TransmitCheck  | Boolean |
| 4603| upce1NumberSystem   | Boolean |
| 4608| upce1CountryCode   | Boolean |
| 4609| upce1ConvertToUpca | Boolean |

## Получение информации о сканировании

Для получения информации о сканировании нужно создать экземпляр абстрактного класса
ru.atol.barcodeservice.api.BarcodeReceiver и переопределить в нем метод onBarcodeReceive. Перед
использованием нужно вызвать метод register(). Каждое сканирование будет вызывать этот метод
и передавать в него структуру Barcode с полями: type (тип ШК) и value (текст отсканированного
ШК). По окончании работы со сканером, например, когда форма сворачивается или закрывается,
нужно вызвать unregister().

#### Пример получения штрих-кода

```
import ru.atol.barcodeservice.api.Barcode
import ru.atol.barcodeservice.api.BarcodeReceiver

class MainActivity : AppCompatActivity() {

    //Экземпляр наследника BarcodeReceiver
    private val barcodeReceiver = object : BarcodeReceiver() {
        override fun onBarcodeReceive(context: Context, barcode: Barcode) {
            //Тип ШК
            scanFragment!!.setBarcodeType(barcode.type)
            //Текст ШК
            scanFragment!!.setBarcodeValue(barcode.value)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        <…>
        //Ре��истрация приемника
        barcodeReceiver.register(this, this)
    }

    <…>
}
```

## Прием штрих-кодов без API

Для того чтобы принимать штрих-коды без библиотеки API, необходимо реализовать
BroadcastReceiver и настроить его на прием сообщения (intent):

- com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST

В нем приходит два поля (extra):

- EXTRA_BARCODE_DECODING_SYMBOLE - тип штрих-кода;
- EXTRA_BARCODE_DECODING_DATA - текст штрих-кода.

#### Полная реализация BarcodeReceiver

```
package ru.atol.barcodeservice.api

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
* Объект для приема отсканированных штрих-кодов.
* Необходимо создать экземпляр наследника класса, переопределив в нем метод onBarcodeReceive()
* Для начала работы нужно вызвать register()
* На каждое сканирование будет вызываться метод onBarcodeReceive()
* Чтобы отключить прием штрих-кодов, нужно вызвать unregister()
*/
abstract class BarcodeReceiver : BroadcastReceiver() {

    companion object {
        const val SCAN_DECODING_BROADCAST = "com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST"
        const val SCAN_DECODING_DATA = "EXTRA_BARCODE_DECODING_DATA"
        const val SCAN_SYMBOLOGY_TYPE = "EXTRA_BARCODE_DECODING_SYMBOLE"
    }

    private var context: Context? = null

    /**
    * Регистрация приемника штри-кодов. Вызывается для начала приема.
    * Если приемник регистрируется этой перегрузкой метода register,
    * в конце работы, при закрытии или сворачивании формы, отключении экрана
    * необходимо вызвать unregister()
    */
    fun register(context: Context) {
        this.context = context
        val xchengIntentFilter = IntentFilter(SCAN_DECODING_BROADCAST)
        context.registerReceiver(this, xchengIntentFilter)
    }

    /**
    * Удаление регистрации приемника. Вызывается для прекращения приема ШК.
    */
    fun unregister(context: Context) {
        this.context = null
        context.unregisterReceiver(this)
    }

    /**
    * Регистрация приемника штри-кодов. Вызывается для начала приема.
    * Если приемник регистрируется этой перегрузкой метода register,
    * в конце работы вызвать unregister() не нужно.
    */
    fun register(lifecycleOwner: LifecycleOwner, context: Context) {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun registerContext() {
                register(context)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun unregisterContext() {
                unregister(context)
            }
        })
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onReceive(context: Context, intent: Intent) {
        when {
            intent.action == SCAN_DECODING_BROADCAST -> if (intent.hasExtra(SCAN_DECODING_DATA)) {
                val type = if (intent.hasExtra(SCAN_SYMBOLOGY_TYPE))
                    intent.getStringExtra(SCAN_SYMBOLOGY_TYPE)
                else
                    "N/A"

                val barcode = Barcode(
                    type ?: "N/A",
                    intent.getStringExtra(SCAN_DECODING_DATA) ?: "N/A"
                )
                onBarcodeReceive(context, barcode)
            }
        }
    }

    /**
    * Прием штрих-кода. Будет вызван при каждом сканировании.
    */
    protected abstract fun onBarcodeReceive(context: Context, barcode: Barcode)
}
```

## Класс Barcode

```
package ru.atol.barcodeservice.api

data class Barcode(
    val type: String,   // Тип ШК: EAN, QR, Code128 и т.д.
    val value: String   // Текстовое значение ШК
)
```