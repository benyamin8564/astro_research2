package com.astroresearch.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val ids = listOf(R.id.name,R.id.mother,R.id.date,R.id.time,R.id.city,R.id.country,R.id.tz,R.id.lat,R.id.lon,R.id.job,R.id.relationship,R.id.horizon,R.id.risk,R.id.question)
        val name=findViewById<EditText>(R.id.name); val mother=findViewById<EditText>(R.id.mother); val date=findViewById<EditText>(R.id.date); val time=findViewById<EditText>(R.id.time); val city=findViewById<EditText>(R.id.city); val country=findViewById<EditText>(R.id.country); val tz=findViewById<EditText>(R.id.tz); val lat=findViewById<EditText>(R.id.lat); val lon=findViewById<EditText>(R.id.lon); val job=findViewById<EditText>(R.id.job); val rel=findViewById<EditText>(R.id.relationship); val horizon=findViewById<EditText>(R.id.horizon); val risk=findViewById<EditText>(R.id.risk); val question=findViewById<EditText>(R.id.question); val result=findViewById<TextView>(R.id.result)
        findViewById<Button>(R.id.analyze).setOnClickListener {
            try {
                val d=java.time.LocalDate.parse(date.text.toString().trim()); val t=java.time.LocalTime.parse(time.text.toString().trim()); val dt=LocalDateTime.of(d,t)
                val tzv=tz.text.toString().trim().replace(",",".").toDouble(); val la=lat.text.toString().trim().replace(",",".").toDoubleOrNull(); val lo=lon.text.toString().trim().replace(",",".").toDoubleOrNull()
                if(la==null||lo==null) { result.text="برای محاسبه دقیق خانه‌ها، عرض و طول جغرافیایی محل تولد را وارد کنید.\n\nمثال تهران: 35.69 و 51.39"; return@setOnClickListener }
                val chart=AstroCalculator.calculate(dt,tzv,la,lo,true)
                result.text=ReportBuilder.build(name.text.toString(),mother.text.toString(),city.text.toString(),country.text.toString(),job.text.toString(),rel.text.toString(),horizon.text.toString(),risk.text.toString(),question.text.toString(),chart)
            } catch(e:Exception){ result.text="خطا در داده‌های ورودی: ${e.message}\nتاریخ باید YYYY-MM-DD و ساعت HH:MM باشد و منطقه زمانی مثل +03:30 یا 3.5 وارد شود." }
        }
        findViewById<Button>(R.id.sources).setOnClickListener { result.text=Sources.text }
    }
}

object ReportBuilder {
    private val signTraits=mapOf("حمل" to "ابتکار، سرعت و رقابت","ثور" to "ثبات، منابع و استمرار","جوزا" to "یادگیری، ارتباط و تنوع","سرطان" to "امنیت، خانواده و مراقبت","اسد" to "خلاقیت، دیده‌شدن و رهبری","سنبله" to "تحلیل، نظم و خدمت","میزان" to "تعادل، مذاکره و مشارکت","عقرب" to "عمق، تمرکز و دگرگونی","قوس" to "گسترش، معنا و تجربه","جدی" to "ساختار، مسئولیت و هدف بلندمدت","دلو" to "نوآوری، شبکه و استقلال","حوت" to "تخیل، همدلی و معنا")
    private val houseTopics=mapOf(1 to "هویت و نحوه حضور شخصی",2 to "منابع، پول و ارزش‌ها",3 to "یادگیری و ارتباط",4 to "خانه و ریشه‌ها",5 to "خلاقیت، عشق و فراغت",6 to "کار روزمره و عادت‌ها",7 to "شراکت و رابطه",8 to "منابع مشترک و ریسک",9 to "تحصیل، سفر و جهان‌بینی",10 to "حرفه و اعتبار",11 to "شبکه‌ها و اهداف آینده",12 to "خلوت، ناخودآگاه و پایان چرخه‌ها")
    fun build(name:String,mother:String,city:String,country:String,job:String,rel:String,horizon:String,risk:String,q:String,c:Chart):String {
        val sb=StringBuilder(); sb.append("گزارش پژوهشی نمودار تولد\n\n"); sb.append("نام: $name\nمحل: $city، $country\n"); if(mother.isNotBlank())sb.append("نام مادر: $mother (اطلاعات هویتی؛ در محاسبه نمودار غربی نقشی ندارد)\n"); sb.append("Ascendant: ${AstroCalculator.sign(c.asc)} ${AstroCalculator.deg(c.asc)}\nMC: ${AstroCalculator.sign(c.mc)} ${AstroCalculator.deg(c.mc)}\n\n")
        sb.append("سیارات و خانه‌ها\n"); c.bodies.forEach{b->sb.append("• ${b.name}: ${AstroCalculator.sign(b.lon)} ${AstroCalculator.deg(b.lon)} — خانه ${b.house} (${houseTopics[b.house]}) — نماد: ${signTraits[AstroCalculator.sign(b.lon)]}\n")}
        sb.append("\nجنبه‌های اصلی\n"); if(c.aspects.isEmpty())sb.append("جنبه اصلی با این دامنه پیدا نشد.\n") else c.aspects.forEach{sb.append("• $it\n")}
        val career=c.bodies.filter{it.house==10||it.house==6||it.house==2}; sb.append("\nحرفه و کار — تفسیر نجومی\n"); career.forEach{sb.append("• ${it.name} در خانه ${it.house}: ${signTraits[AstroCalculator.sign(it.lon)]}. این فقط یک الگوی نمادین برای بررسی مسیرهای کاری است، نه توصیه شغلی قطعی.\n")}
        sb.append("\nمالی و سرمایه‌گذاری — تفسیر نجومی\n"); sb.append("خانه ۲، ۸ و ۱۱ و سیارات مرتبط در این گزارش به‌عنوان موضوعات مالی/منابع دیده شده‌اند. از دید نجومی، این بخش فقط برای ساخت فرضیه درباره سبک ریسک، منابع مشترک و اهداف مالی است.\n")
        sb.append("افق اعلام‌شده: ${horizon.ifBlank{"نامشخص"}} | تحمل ریسک: ${risk.ifBlank{"نامشخص"}}/10\n"); sb.append("هیچ سهم، رمزارز، صندوق یا معامله مشخصی بر اساس این نمودار توصیه نمی‌شود. برای تصمیم مالی واقعی از داده‌های مالی و مشاور دارای مجوز استفاده کنید.\n")
        sb.append("\nسلامت — تفسیر نمادین، نه تشخیص\n"); val health=c.bodies.filter{it.house==1||it.house==6||it.house==12}; health.forEach{sb.append("• ${it.name} در خانه ${it.house}: در سنت‌های نجومی با موضوعات ${houseTopics[it.house]} پیوند داده می‌شود. این مورد بیماری را پیش‌بینی یا تشخیص نمی‌دهد.\n")}; sb.append("اگر نگرانی پزشکی وجود دارد، تشخیص باید با پزشک و آزمایش مستقل انجام شود.\n")
        sb.append("\nپرسش‌های اعتبارسنجی برای مصاحبه\n• آیا الگوی شغلی/زندگی مرتبط با ${signTraits[AstroCalculator.sign(c.asc)]} را در تجربه واقعی خود می‌بینید؟\n• آیا موضوع ${houseTopics[10]} در زندگی شما پررنگ بوده است؟\n• آیا موضوع ${houseTopics[7]} یا ${houseTopics[2]} با سؤال اصلی شما ارتباط دارد؟\n• پاسخ‌ها را بعد از ثبت این گزارش جمع‌آوری کنید تا سوگیری تأییدی کمتر شود.\n")
        sb.append("\nروش و محدودیت‌ها\nاین برنامه از نمودار تروپیکال استفاده می‌کند، خانه‌ها را به روش Whole Sign می‌چیند و موقعیت سیارات را با تقریب نجومی داخلی محاسبه می‌کند. برای پژوهش دانشگاهی یا انتشار، نتایج را با یک اپمریس معتبر مثل Swiss Ephemeris/سرویس محاسباتی مستقل تطبیق دهید. محاسبات نجومی با باورهای تفسیری یکی نیستند.\n\n")
        sb.append("سؤال اصلی ثبت‌شده: ${q.ifBlank{"—"}}\nشغل: ${job.ifBlank{"—"}} | رابطه: ${rel.ifBlank{"—"}}\n\n")
        sb.append("هشدار: آسترولوژی از نظر علمی برای تشخیص بیماری یا تضمین موفقیت مالی اثبات نشده است. این برنامه ابزار تفسیر/پژوهش درباره سنت‌های آسترولوژیک است، نه ابزار پزشکی یا مالی.\n")
        return sb.toString()
    }
}

object Sources { val text="""منابع و روش‌شناسی\n\n1) Astro.com / Astrodienst — توضیح ساختار نمودار تولد: زودیاک، خانه‌ها، سیارات، جنبه‌ها و محورها.\nhttps://www.astro.com/astrology/in_chart_e.htm\n\n2) Astro.com — فرم داده تولد: تاریخ، ساعت دقیق، شهر/کشور و منطقه زمانی و اهمیت اعمال اصلاحات منطقه زمانی.\nhttps://www.astro.com/astrology/cgi/ade.cgi\n\n3) Swiss Ephemeris — موتور محاسبات نجومی مورد استفاده در بسیاری از نرم‌افزارهای آسترولوژی؛ مجوز آن برای استفاده در نرم‌افزار باید جداگانه بررسی شود.\nhttps://www.astro.com/swisseph/\n\n4) شواهد علمی: یک مطالعه جمعیتی بسیار بزرگ روی ارتباط نشانه‌های نجومی و تشخیص‌های بیمارستانی نشان داد ارتباط‌های اولیه پس از اصلاح آزمون‌های متعدد پایدار نبودند.\nJournal of Clinical Epidemiology (2006), PMID/DOI قابل جستجو با عنوان مطالعه.\n\nنکته: این برنامه داده‌های نجومی را محاسبه و تفسیرهای سنتی را ارائه می‌کند؛ ادعاهای پزشکی و مالی را به‌عنوان واقعیت علمی معرفی نمی‌کند.""" }
