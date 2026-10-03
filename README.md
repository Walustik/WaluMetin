# WaluMetin - Metin Taşı Plugin

Spigot/Paper Minecraft eklentisi - Dinamik metin taşı sistemi (Aşama 1 / Beta)

## Özellikler

### ✅ Aşama 1 (Beta) Gereksinimleri

- **Dinamik Taş Tanımlama**: `config.yml` dosyasında birden fazla metin taşı türü tanımlanabilir
- **Yasaklı Blok Koruması**: Arayüz açan bloklar (Çalışma Masası, Çekiç, Sandık, vb.) ve dekoratif bloklar (Çimen, Çiçek, vb.) metin taşı olarak seçilemez
- **Hasar Mekaniği**: Oyuncular bloğa vurdukça HP azalır, blok kırılmaz
- **Hologram Göstergesi**: Blok üzerinde ArmorStand tabanlı hologram ile canlı HP göstergesi
- **Renk Desteği**: Hem klasik renk kodları (`&a`, `&l`) hem de Hex kodları (`&#FF5555`) desteklenir
- **Can Gösterimi**: Her vurduğunda ActionBar'da kalan can bilgisi gösterilir
- **Yeniden Doğma Döngüsü**: Can 0 olduğunda BEDROCK'a dönüşür, belirtilen süre sonra orijinal türe döner
- **Admin Komutu**: `/metintas olustur <tasAdi>` komutu ile metin taşı oluşturma

## Sistem Gereksinimleri

- **Platform**: Paper/Spigot Minecraft Sunucusu
- **Sürüm**: 1.20.4 (1.20+ ile 1.26+ arası uyumlu)
- **Java**: Java 21+
- **Build**: Gradle

## Kurulum

### 1. Plugin'i Build Etme

```bash
# Repository'i klonla
git clone https://github.com/Walustik/WaluMetin.git
cd WaluMetin

# Gradle ile build et
./gradlew build

# JAR dosyası şurada oluşturulur:
# build/libs/WaluMetin-1.0.0.jar
```

### 2. Sunucuya Yükleme

```bash
# JAR dosyasını plugins klasörüne kopyala
cp build/libs/WaluMetin-1.0.0.jar /path/to/server/plugins/

# Sunucuyu başlat
./start.sh
```

## Konfigürasyon

### config.yml Örneği

Plugin ilk çalışmada `plugins/WaluMetin/config.yml` dosyasını otomatik oluşturur.

```yaml
MetinTaslari:
  ornek_tas:
    Name: "&6&lElmas Metin Taşı"
    Type: "DIAMOND_ORE"
    HP: 100
    Duration: 30  # Yenileme süresi (saniye)

  altin_tas:
    Name: "&e&lAltın Metin Taşı"
    Type: "GOLD_ORE"
    HP: 80
    Duration: 25

  demir_tas:
    Name: "&7&lDemir Metin Taşı"
    Type: "IRON_ORE"
    HP: 60
    Duration: 20
```

### Renk Kodları

**Klasik Kodlar:**
- `&0` - Siyah
- `&1` - Koyu Mavi
- `&2` - Koyu Yeşil
- `&3` - Türküaz
- `&4` - Koyu Kırmızı
- `&5` - Mor
- `&6` - Altın
- `&7` - Gri
- `&8` - Koyu Gri
- `&9` - Mavi
- `&a` - Yeşil
- `&b` - Su Mavisi
- `&c` - Kırmızı
- `&d` - Şeker Pembesi
- `&e` - Sarı
- `&f` - Beyaz
- `&l` - **Kalın**
- `&m` - ~~Üstü Çizili~~
- `&n` - <u>Altı Çizili</u>
- `&o` - *İtalik*
- `&k` - Gizli
- `&r` - Reset

**Hex Kodlar:**
- `&#FF5555` - Kırmızı Ton
- `&#55FF55` - Yeşil Ton
- `&#5555FF` - Mavi Ton

## Komutlar

### /metintas olustur <tasAdi>
Oyuncunun baktığı bloğu metin taşına dönüştürür.

```
/metintas olustur ornek_tas
```

### /metintas olustur <tasAdi> [x y z]
Belirtilen koordinata metin taşı oluşturur.

```
/metintas olustur ornek_tas 100 64 200
```

## İzinler

- `walumetin.admin` - Metin taşı komutlarını kullanma (Varsayılan: OP)

## Dosya Yapısı

```
WaluMetin/
├── build.gradle                                    # Gradle konfigürasyonu
├── settings.gradle
├── .gitignore
├── README.md
└── src/
    └── main/
        ├── java/com/walustik/walumetin/
        │   ├── WaluMetinPlugin.java               # Ana sınıf
        │   ├── util/
        │   │   └── ColorUtil.java                 # Renk kodları
        │   ├── config/
        │   │   ├── TextStoneConfigManager.java    # Config yöneticisi
        │   │   └── TextStoneDefinition.java       # Metin taşı tanımı
        │   ├── model/
        │   │   └── TextStoneState.java            # Metin taşı durumu
        │   ├── service/
        │   │   └── TextStoneService.java          # Ana servis/logic
        │   ├── listener/
        │   │   └── TextStoneProtectionListener.java # Event dinleyici
        │   └── command/
        │       └── TextStoneCommand.java          # Komut yöneticisi
        └── resources/
            ├── plugin.yml                         # Plugin meta
            └── config.yml                         # Varsayılan config
```

## Çalışma Mantığı

### 1. Metin Taşı Oluşturma
1. Admin `/metintas olustur <tasAdi>` komutunu çalıştırır
2. Config'den taş tanımı yüklenir
3. Blok belirtilen türe dönüştürülür
4. Hologram blok üzerine yerleştirilir
5. Metin taşı harita veya memory'de kaydedilir

### 2. Hasar Alma
1. Oyuncu metin taşına sağ tık/vurma yapar
2. `BlockDamageEvent` tetiklenir
3. HP 1 azalır
4. ActionBar'da yeni HP gösterilir
5. Hologram güncellenir

### 3. Kırılma
1. HP 0'a ulaşır
2. Blok BEDROCK'a dönüştürülür
3. Yenileme süresi başlar
4. Hologram "Yenileniyor..." gösterilir
5. Tick task her saniye kontrol eder

### 4. Yenileme
1. Belirtilen süre geçer
2. Blok orijinal türe döner
3. HP full olarak sıfırlanır
4. Hologram güncellenir
5. Döngü tekrarlanır

## Teknik Detaylar

### Sınıf Mimarisi

- **WaluMetinPlugin**: Başlatma ve kapatma
- **TextStoneConfigManager**: Config dosyası yönetimi
- **TextStoneService**: Ana iş mantığı (hasar, yenileme, hologram)
- **TextStoneProtectionListener**: Event işleyici
- **TextStoneCommand**: Komut işleyici
- **TextStoneState**: Metin taşı veri modeli
- **TextStoneDefinition**: Metin taşı tanım şablonu
- **ColorUtil**: Renk kodları dönüştürücü

### Veri Yapısı

Metin taşları bellekte `HashMap<String, TextStoneState>` yapısında saklanır.
- Anahtar: `world:x:y:z`
- Değer: TextStoneState nesnesi

### Tick Sistemi

- 20 tick/saniye (1 saniye)
- Her saniye yenileniyor durumundaki taşları kontrol eder
- Süre biterse restore işlemi yapılır

## Bilinen Sınırlamalar (Aşama 1)

- ❌ Veritabanı/Kalıcılık desteği yok (server yeniden başlarsa sıfırlanır)
- ❌ Metin taşı istatistikleri kaydı yok
- ❌ Çoklu dünya tam desteği (Başlangıç desteği var)
- ❌ Özel efekt/ses desteği yok
- ❌ NPC/Bot desteği yok
- ❌ Sınavlar/Görevler sistemi yok

## Planlanan Özellikleri (Aşama 2+)

- 📊 Veritabanı (SQLite/MySQL) entegrasyonu
- 📈 İstatistik ve liderlik tablosu
- 🎨 Efekt ve parçacık sistemi
- 🔊 Ses ve müzik desteği
- 👥 Çok oyuncu etkinlikleri
- 📝 Türkçe yerelleştirme tamamen
- 🛡️ Doğrudan blok tutturma/kaydetme

## Sorun Giderme

### Plugin başlamıyor
```
[WARN] java.lang.UnsupportedClassVersionError: ...
```
**Çözüm**: Java 21+ gereklidir. `java -version` ile kontrol edin.

### Config dosyası oluşturulmuyor
```
[WARN] MetinTaslari bölümü bulunamadı.
```
**Çözüm**: Plugin klasörünü silin ve sunucuyu yeniden başlatın. Default config otomatik oluşturulacak.

### Metin taşı oluşturulamıyor
```
[WARN] Geçersiz metin taşı türü: INVALID_TYPE
```
**Çözüm**: Config'de `Type:` değeri geçerli bir blok türü olmalıdır. Örn: `DIAMOND_ORE`

## Katkı

Buglar ve öneriler için GitHub Issues'ı kullanın.

## Lisans

MIT Lisansı

## Geliştirici

**Walustik** - [GitHub](https://github.com/Walustik)

---

**Sürüm**: 1.0.0 (Beta)  
**Güncellenme**: 2026
