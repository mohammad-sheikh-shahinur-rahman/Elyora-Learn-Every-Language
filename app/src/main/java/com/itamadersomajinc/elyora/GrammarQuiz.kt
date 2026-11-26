package com.itamadersomajinc.elyora

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class GrammarQuizMode(val label: String) {
    MIXED("Mixed · মিশ্র"),
    MCQ("MCQ · বহুনির্বাচনি"),
    FILL("Fill Blanks · শূন্যস্থান"),
    CORRECTION("Correction · ভুল সংশোধন")
}

data class GrammarQuestion(
    val id: String,
    val type: GrammarQuizMode,
    val topic: String,
    val prompt: String,
    val options: List<String>,
    val answer: String,
    val explanation: String,
    val explanationBn: String,
    val audioText: String = ""
)

object GrammarQuizBank {
    val topics = listOf("All", "Tenses", "Prepositions", "Articles", "Agreement", "Conditionals", "Modals & Passive")

    fun topicBn(topic: String): String = when (topic) {
        "All" -> "সব"
        "Tenses" -> "টেন্স"
        "Prepositions" -> "অব্যয়"
        "Articles" -> "আর্টিকেল"
        "Agreement" -> "কর্তা-ক্রিয়া"
        "Conditionals" -> "শর্তবাক্য"
        "Modals & Passive" -> "মডাল ও প্যাসিভ"
        else -> topic
    }

    val curated: List<GrammarQuestion> = listOf(
        // MCQ
        GrammarQuestion("q1", GrammarQuizMode.MCQ, "Tenses", "She ___ in London since 2018.", listOf("lives", "lived", "has lived", "is living"), "has lived", "Use Present Perfect with 'since + year' for states continuing until now.", "'since + সাল' থাকলে অতীত থেকে এখনো চলমান অবস্থা বোঝাতে Present Perfect (has lived) বসে।", "She has lived in London since 2018."),
        GrammarQuestion("q2", GrammarQuizMode.MCQ, "Tenses", "When I arrived, they ___ dinner.", listOf("have", "were having", "are having", "will have"), "were having", "Past Continuous describes the background action interrupted by Past Simple.", "অতীতে চলমান পটভূমির কাজের মাঝে অন্য ঘটনা ঘটলে Past Continuous (were having) বসে।", "When I arrived, they were having dinner."),
        GrammarQuestion("q3", GrammarQuizMode.MCQ, "Tenses", "By next month, we ___ the course.", listOf("finish", "finished", "will have finished", "are finishing"), "will have finished", "'By next month' signals completion before a future deadline -> Future Perfect.", "'By next month' ভবিষ্যতের নির্দিষ্ট সময়ের মধ্যে শেষ হওয়া কাজ বোঝায় -> Future Perfect।", "By next month, we will have finished the course."),
        GrammarQuestion("q4", GrammarQuizMode.MCQ, "Tenses", "Water ___ at 100 degrees Celsius.", listOf("boil", "boils", "is boiling", "boiled"), "boils", "General scientific facts always use Present Simple.", "চিরন্তন ও বৈজ্ঞানিক সত্যে সবসময় Present Simple (boils) বসে।", "Water boils at 100 degrees Celsius."),
        GrammarQuestion("q5", GrammarQuizMode.MCQ, "Tenses", "They ___ each other for ten years before they got married.", listOf("know", "have known", "had known", "were knowing"), "had known", "Earlier past duration before another past event -> Past Perfect (state verb 'know').", "অতীতের আরেকটি ঘটনার আগের সময়কাল বোঝাতে Past Perfect (had known) বসে।", "They had known each other for ten years before they got married."),
        GrammarQuestion("q6", GrammarQuizMode.MCQ, "Prepositions", "The meeting starts ___ 9:30 AM ___ Monday.", listOf("in / on", "at / on", "on / at", "at / in"), "at / on", "Use 'at' for exact clock times and 'on' for days of the week.", "নির্দিষ্ট ঘড়ির সময়ের আগে 'at' এবং বারের নামের আগে 'on' বসে।", "The meeting starts at 9:30 AM on Monday."),
        GrammarQuestion("q7", GrammarQuizMode.MCQ, "Prepositions", "She is very good ___ solving math problems.", listOf("in", "at", "on", "for"), "at", "The fixed collocation is 'good at + noun / V-ing'.", "দক্ষতা বোঝাতে সঠিক শব্দজোড় হলো 'good at'।", "She is very good at solving math problems."),
        GrammarQuestion("q8", GrammarQuizMode.MCQ, "Prepositions", "I am looking forward ___ seeing you soon.", listOf("for", "at", "to", "with"), "to", "'Look forward to' uses 'to' as a preposition followed by V-ing.", "'Look forward to'-এর সাথে 'to' বসে এবং এর পরে ভার্বটি V-ing হয়।", "I am looking forward to seeing you soon."),
        GrammarQuestion("q9", GrammarQuizMode.MCQ, "Prepositions", "He has been absent ___ class for three days.", listOf("from", "of", "in", "at"), "from", "The correct preposition is 'absent from'.", "অনুপস্থিত থাকা বোঝাতে 'absent from' ব্যবহৃত হয়।", "He has been absent from class for three days."),
        GrammarQuestion("q10", GrammarQuizMode.MCQ, "Articles", "She wants to study at ___ university in Europe.", listOf("a", "an", "the", "no article"), "a", "'University' begins with a consonant sound /ju:/, so use 'a'.", "'University'-এর উচ্চারণ 'ইউ' (ব্যঞ্জনধ্বনি) দিয়ে শুরু হয়, তাই 'a' বসে।", "She wants to study at a university in Europe."),
        GrammarQuestion("q11", GrammarQuizMode.MCQ, "Articles", "He waited for ___ hour in the rain.", listOf("a", "an", "the", "no article"), "an", "The 'h' in 'hour' is silent, so it starts with a vowel sound -> 'an hour'.", "'Hour'-এ 'h' অনুচ্চারিত থাকে এবং স্বরধ্বনি দিয়ে শুরু হয়, তাই 'an hour' বসে।", "He waited for an hour in the rain."),
        GrammarQuestion("q12", GrammarQuizMode.MCQ, "Articles", "___ Nile is the longest river in Africa.", listOf("A", "An", "The", "No article"), "The", "Use 'the' before names of rivers, oceans, and seas.", "নদী, সাগর ও মহাসাগরের নামের আগে 'The' বসে।", "The Nile is the longest river in Africa."),
        GrammarQuestion("q13", GrammarQuizMode.MCQ, "Agreement", "Each of the students ___ given a certificate.", listOf("were", "are", "was", "have"), "was", "'Each of + plural noun' takes a singular verb (was).", "'Each of + বহুবচন নাউন' থাকলে ভার্ব একবচন (was) হয়।", "Each of the students was given a certificate."),
        GrammarQuestion("q14", GrammarQuizMode.MCQ, "Agreement", "Neither the manager nor the employees ___ ready.", listOf("is", "was", "were", "has been"), "were", "With 'neither ... nor', the verb agrees with the nearer subject ('employees' -> plural).", "'neither ... nor'-এ ভার্ব কাছের সাবজেক্ট ('employees' - বহুবচন) অনুযায়ী 'were' হয়।", "Neither the manager nor the employees were ready."),
        GrammarQuestion("q15", GrammarQuizMode.MCQ, "Agreement", "The news from the coast ___ very encouraging.", listOf("are", "were", "is", "have been"), "is", "'News' is an uncountable singular noun -> takes 'is'.", "'News' দেখতে বহুবচন মনে হলেও এটি একবচন, তাই 'is' বসে।", "The news from the coast is very encouraging."),
        GrammarQuestion("q16", GrammarQuizMode.MCQ, "Conditionals", "If it ___ tomorrow, we will cancel the picnic.", listOf("will rain", "rains", "rained", "raining"), "rains", "First Conditional uses Present Simple in the 'if' clause, never 'will'.", "First Conditional-এ 'if' অংশে Present Simple (rains) বসে, কখনো 'will' নয়।", "If it rains tomorrow, we will cancel the picnic."),
        GrammarQuestion("q17", GrammarQuizMode.MCQ, "Conditionals", "If I ___ you, I would accept the scholarship.", listOf("am", "was", "were", "will be"), "were", "Second Conditional uses 'were' for all subjects in formal English ('If I were you').", "অবাস্তব কল্পনা (Second Conditional)-এ সব সাবজেক্টের সাথে 'were' বসে।", "If I were you, I would accept the scholarship."),
        GrammarQuestion("q18", GrammarQuizMode.MCQ, "Conditionals", "If she had studied harder, she ___ the exam.", listOf("will pass", "would pass", "would have passed", "passed"), "would have passed", "Third Conditional: If + had + V3, ... would have + V3.", "Third Conditional: If + had + V3 থাকলে ফলাফল অংশে would have + V3 বসে।", "If she had studied harder, she would have passed the exam."),
        GrammarQuestion("q19", GrammarQuizMode.MCQ, "Modals & Passive", "You ___ wear a seatbelt while driving; it's the law.", listOf("might", "must", "could", "would"), "must", "'Must' expresses strong obligation or legal rule.", "আইনি বাধ্যবাধকতা বা জরুরি নিয়ম বোঝাতে 'must' বসে।", "You must wear a seatbelt while driving; it's the law."),
        GrammarQuestion("q20", GrammarQuizMode.MCQ, "Modals & Passive", "This bridge ___ in 1995.", listOf("built", "was built", "is built", "has built"), "was built", "Past Simple Passive: was/were + V3.", "অতীতের কর্মবাচ্য (Past Passive): was/were + V3 বসে।", "This bridge was built in 1995."),
        GrammarQuestion("q21", GrammarQuizMode.MCQ, "Modals & Passive", "The letters ___ right now by the assistant.", listOf("are typed", "are being typed", "were typing", "have typed"), "are being typed", "Present Continuous Passive: am/is/are + being + V3.", "বর্তমানে চলমান কর্মবাচ্যে am/is/are + being + V3 বসে।", "The letters are being typed right now by the assistant."),
        GrammarQuestion("q22", GrammarQuizMode.MCQ, "Agreement", "Mathematics ___ my favorite subject at school.", listOf("are", "were", "is", "be"), "is", "Academic subjects ending in -ics (mathematics, physics) take a singular verb.", "Mathematics বা Physics-এর মতো বিষয়ের নামের পরে একবচন ভার্ব 'is' বসে।", "Mathematics is my favorite subject at school."),

        // FILL IN THE BLANKS
        GrammarQuestion("f1", GrammarQuizMode.FILL, "Tenses", "Yesterday, he ___ (go) to the library early.", listOf("went", "gone", "goes", "going"), "went", "Past Simple (V2) of irregular verb 'go' is 'went'.", "'go' ক্রিয়ার Past Simple (V2) রূপ হলো 'went'।", "Yesterday, he went to the library early."),
        GrammarQuestion("f2", GrammarQuizMode.FILL, "Tenses", "She has never ___ (see) the ocean before.", listOf("seen", "saw", "see", "seeing"), "seen", "Present Perfect requires V3 (past participle): see -> saw -> seen.", "Present Perfect-এ has-এর পর V3 ('seen') বসে।", "She has never seen the ocean before."),
        GrammarQuestion("f3", GrammarQuizMode.FILL, "Tenses", "Right now, the children are ___ (swim) in the pool.", listOf("swimming", "swiming", "swam", "swum"), "swimming", "CVC short vowel verb 'swim' doubles the final 'm' before -ing.", "'swim'-এর শেষে স্বল্প স্বরধ্বনি+ব্যঞ্জন থাকায় -ing যোগে 'm' দ্বিত্ব হয়ে 'swimming' হয়।", "Right now, the children are swimming in the pool."),
        GrammarQuestion("f4", GrammarQuizMode.FILL, "Prepositions", "We have lived in this house ___ five years.", listOf("for", "since", "during", "from"), "for", "Use 'for' with a duration/length of time ('five years').", "মোট সময়ের ব্যাপ্তি ('পাঁচ বছর ধরে') বোঝাতে 'for' বসে।", "We have lived in this house for five years."),
        GrammarQuestion("f5", GrammarQuizMode.FILL, "Prepositions", "She has been studying English ___ 2021.", listOf("since", "for", "in", "by"), "since", "Use 'since' with a specific starting point in time.", "শুরুর নির্দিষ্ট সময় ('২০২১ সাল থেকে') বোঝাতে 'since' বসে।", "She has been studying English since 2021."),
        GrammarQuestion("f6", GrammarQuizMode.FILL, "Articles", "He is ___ honest and hardworking man.", listOf("an", "a", "the", "one"), "an", "'Honest' begins with a silent 'h' (vowel sound) -> use 'an'.", "'Honest'-এ 'h' অনুচ্চারিত, তাই 'an' বসে।", "He is an honest and hardworking man."),
        GrammarQuestion("f7", GrammarQuizMode.FILL, "Conditionals", "If I had more free time, I ___ learn Spanish.", listOf("would", "will", "had", "can"), "would", "Second Conditional uses 'would + base verb' in the result clause.", "Second Conditional-এর ফলাফল অংশে 'would + V1' বসে।", "If I had more free time, I would learn Spanish."),
        GrammarQuestion("f8", GrammarQuizMode.FILL, "Modals & Passive", "English is ___ (speak) in many countries.", listOf("spoken", "spoke", "speak", "speaking"), "spoken", "Present Simple Passive uses is/are + V3 ('spoken').", "Present Passive গঠনে is/are + V3 ('spoken') বসে।", "English is spoken in many countries."),
        GrammarQuestion("f9", GrammarQuizMode.FILL, "Agreement", "Everyone ___ (want) to succeed in life.", listOf("wants", "want", "wanting", "wanted"), "wants", "Indefinite pronouns like 'everyone', 'somebody', 'nobody' take singular verbs.", "'Everyone', 'nobody'-এর মতো শব্দগুলো একবচন ভার্ব (wants) গ্রহণ করে।", "Everyone wants to succeed in life."),
        GrammarQuestion("f10", GrammarQuizMode.FILL, "Tenses", "Did you ___ (write) that email yesterday?", listOf("write", "wrote", "written", "writing"), "write", "After auxiliary 'Did', always use the base form V1 ('write').", "'Did'-এর পরে সবসময় ভার্বের মূল রূপ V1 ('write') বসে।", "Did you write that email yesterday?"),
        GrammarQuestion("f11", GrammarQuizMode.FILL, "Prepositions", "She is interested ___ learning modern literature.", listOf("in", "on", "at", "with"), "in", "The fixed collocation is 'interested in'.", "আগ্রহী বোঝাতে সবসময় 'interested in' ব্যবহৃত হয়।", "She is interested in learning modern literature."),
        GrammarQuestion("f12", GrammarQuizMode.FILL, "Modals & Passive", "You ___ (should / mustn't) drink plenty of water every day.", listOf("should", "mustn't", "couldn't", "wouldn't"), "should", "'Should' is used for friendly advice and healthy recommendations.", "উপদেশ বা ভালো পরামর্শ দিতে 'should' বসে।", "You should drink plenty of water every day."),
        GrammarQuestion("f13", GrammarQuizMode.FILL, "Tenses", "They had already ___ (eat) lunch before we arrived.", listOf("eaten", "ate", "eat", "eating"), "eaten", "Past Perfect uses had + V3 (eat -> ate -> eaten).", "Past Perfect-এ had + V3 ('eaten') বসে।", "They had already eaten lunch before we arrived."),
        GrammarQuestion("f14", GrammarQuizMode.FILL, "Prepositions", "The train arrived ___ the station right on time.", listOf("at", "in", "to", "on"), "at", "Use 'arrive at' for buildings/stations and 'arrive in' for cities/countries.", "স্টেশন বা নির্দিষ্ট স্থানে পৌঁছাতে 'arrive at' এবং বড় শহরে/দেশে 'arrive in' বসে।", "The train arrived at the station right on time."),

        // ERROR CORRECTION
        GrammarQuestion("c1", GrammarQuizMode.CORRECTION, "Tenses", "Fix the mistake · ভুল ঠিক করুন:\n\"She don't like spicy food.\"", listOf("She doesn't like spicy food.", "She doesn't likes spicy food.", "She not like spicy food.", "She isn't like spicy food."), "She doesn't like spicy food.", "3rd person singular negative uses 'doesn't + base verb'.", "তৃতীয় পুরুষ একবচনে না-বোধক করতে 'doesn't + মূল ভার্ব' বসে।", "She doesn't like spicy food."),
        GrammarQuestion("c2", GrammarQuizMode.CORRECTION, "Tenses", "Fix the mistake · ভুল ঠিক করুন:\n\"I have seen him yesterday.\"", listOf("I saw him yesterday.", "I had seen him yesterday.", "I am seeing him yesterday.", "I see him yesterday."), "I saw him yesterday.", "Never use Present Perfect with finished past time words like 'yesterday'.", "'yesterday' থাকলে Present Perfect নয়, Past Simple ('saw') বসে।", "I saw him yesterday."),
        GrammarQuestion("c3", GrammarQuizMode.CORRECTION, "Tenses", "Fix the mistake · ভুল ঠিক করুন:\n\"Did he went to college today?\"", listOf("Did he go to college today?", "Does he went to college today?", "Was he go to college today?", "Did he goes to college today?"), "Did he go to college today?", "After 'Did', use the base verb 'go', not 'went'.", "'Did'-এর পর মূল ভার্ব 'go' বসে, 'went' নয়।", "Did he go to college today?"),
        GrammarQuestion("c4", GrammarQuizMode.CORRECTION, "Agreement", "Fix the mistake · ভুল ঠিক করুন:\n\"One of my friends live in Canada.\"", listOf("One of my friends lives in Canada.", "One of my friend live in Canada.", "One of my friends are living in Canada.", "One of my friends living in Canada."), "One of my friends lives in Canada.", "The head subject is singular 'One', so the verb must be 'lives'.", "মূল সাবজেক্ট 'One' (একবচন), তাই ভার্ব 'lives' হবে।", "One of my friends lives in Canada."),
        GrammarQuestion("c5", GrammarQuizMode.CORRECTION, "Prepositions", "Fix the mistake · ভুল ঠিক করুন:\n\"She is married with a doctor.\"", listOf("She is married to a doctor.", "She is married by a doctor.", "She is married at a doctor.", "She married to a doctor."), "She is married to a doctor.", "Use 'married to' (adjective) or 'marry someone' (verb, no preposition).", "সঠিক ব্যবহার হলো 'married to', কখনো 'married with' নয়।", "She is married to a doctor."),
        GrammarQuestion("c6", GrammarQuizMode.CORRECTION, "Prepositions", "Fix the mistake · ভুল ঠিক করুন:\n\"We discussed about the new plan.\"", listOf("We discussed the new plan.", "We discussed on the new plan.", "We discussed for the new plan.", "We discussed to the new plan."), "We discussed the new plan.", "'Discuss' is transitive and takes a direct object without 'about'.", "'Discuss'-এর পর কখনো 'about' বসে না; সরাসরি object বসে।", "We discussed the new plan."),
        GrammarQuestion("c7", GrammarQuizMode.CORRECTION, "Articles", "Fix the mistake · ভুল ঠিক করুন:\n\"He gave me an useful advice.\"", listOf("He gave me useful advice.", "He gave me a useful advices.", "He gave me the useful advices.", "He gave me an advices."), "He gave me useful advice.", "'Advice' is uncountable — never use 'a/an' or plural 'advices' with it.", "'Advice' অগণনাযোগ্য (uncountable), তাই এর সাথে a/an বা advices বসে না।", "He gave me useful advice."),
        GrammarQuestion("c8", GrammarQuizMode.CORRECTION, "Conditionals", "Fix the mistake · ভুল ঠিক করুন:\n\"If I will see him, I will tell him.\"", listOf("If I see him, I will tell him.", "If I saw him, I will tell him.", "If I seeing him, I will tell him.", "If I would see him, I tell him."), "If I see him, I will tell him.", "Never put 'will' inside the 'if' clause of a First Conditional.", "First Conditional-এ 'If' অংশে কখনো 'will' বসে না।", "If I see him, I will tell him."),
        GrammarQuestion("c9", GrammarQuizMode.CORRECTION, "Modals & Passive", "Fix the mistake · ভুল ঠিক করুন:\n\"He can speaks three languages.\"", listOf("He can speak three languages.", "He can to speak three languages.", "He cans speak three languages.", "He can speaking three languages."), "He can speak three languages.", "Modal verbs (can, must, should, will) are always followed by the bare infinitive.", "Modal verb-এর পর ভার্বের সাথে কখনো s/es বা ing যুক্ত হয় না।", "He can speak three languages."),
        GrammarQuestion("c10", GrammarQuizMode.CORRECTION, "Agreement", "Fix the mistake · ভুল ঠিক করুন:\n\"There is many books on the table.\"", listOf("There are many books on the table.", "There has many books on the table.", "There was many books on the table.", "There be many books on the table."), "There are many books on the table.", "Plural noun 'many books' requires 'There are'.", "'many books' বহুবচন হওয়ায় 'There are' বসবে।", "There are many books on the table."),
        GrammarQuestion("c11", GrammarQuizMode.CORRECTION, "Prepositions", "Fix the mistake · ভুল ঠিক করুন:\n\"I prefer coffee than tea.\"", listOf("I prefer coffee to tea.", "I prefer coffee from tea.", "I prefer coffee over than tea.", "I prefer coffee by tea."), "I prefer coffee to tea.", "Always pair 'prefer' with 'to' (prefer A to B), never 'than'.", "'Prefer'-এর সাথে সবসময় 'to' বসে, 'than' নয়।", "I prefer coffee to tea."),
        GrammarQuestion("c12", GrammarQuizMode.CORRECTION, "Tenses", "Fix the mistake · ভুল ঠিক করুন:\n\"I am living here since 2020.\"", listOf("I have been living here since 2020.", "I live here since 2020.", "I was living here since 2020.", "I lived here since 2020."), "I have been living here since 2020.", "Use Present Perfect Continuous (or Present Perfect) with 'since' for actions continuing to now.", "'since' থাকলে বর্তমান পর্যন্ত চলা কাজের জন্য Present Perfect Continuous বসে।", "I have been living here since 2020.")
    )

    fun buildSession(type: GrammarQuizMode, topic: String, count: Int = 10): List<GrammarQuestion> {
        val pool = curated.filter { q ->
            (type == GrammarQuizMode.MIXED || q.type == type) &&
                (topic == "All" || q.topic == topic)
        }.toMutableList()

        if ((topic == "All" || topic == "Tenses") && Verbs.all.isNotEmpty()) {
            val irregulars = Verbs.all.filter { it.irregular && it.v2.isNotBlank() && it.v3.isNotBlank() && it.v2 != it.base }
                .shuffled().take(4)
            irregulars.forEachIndexed { idx, v ->
                if (type == GrammarQuizMode.MIXED || type == GrammarQuizMode.MCQ) {
                    val wrong1 = v.base + "ed"
                    val wrong2 = v.v4
                    val wrong3 = if (v.v3 != v.v2) v.v3 else v.base + "s"
                    val opts = listOf(v.v2, wrong1, wrong2, wrong3).distinct()
                    if (opts.size == 4) {
                        val bnMeaning = Verbs.meaning(v, "bn")
                        pool.add(
                            GrammarQuestion(
                                id = "dyn_v2_${v.base}_$idx",
                                type = GrammarQuizMode.MCQ,
                                topic = "Tenses",
                                prompt = "Choose Past Simple (V2) of \"${v.base}\" ($bnMeaning):",
                                options = opts.shuffled(),
                                answer = v.v2,
                                explanation = "Irregular verb: ${v.base} → ${v.v2} (V2) → ${v.v3} (V3).",
                                explanationBn = "অনিয়মিত ক্রিয়া '$bnMeaning': ${v.base} (V1) → ${v.v2} (V2) → ${v.v3} (V3)।",
                                audioText = "${v.base}, ${v.v2}, ${v.v3}"
                            )
                        )
                    }
                }
            }
        }
        return pool.shuffled().take(count)
    }
}

@Composable
fun GrammarQuizScreen(
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    var selectedType by remember { mutableStateOf(GrammarQuizMode.MIXED) }
    var selectedTopic by remember { mutableStateOf("All") }
    var activeQuestions by remember { mutableStateOf<List<GrammarQuestion>?>(null) }

    if (activeQuestions != null) {
        GrammarQuizRunner(
            title = "${selectedType.label} · ${GrammarQuizBank.topicBn(selectedTopic)}",
            questions = activeQuestions!!,
            store = store,
            speaker = speaker,
            modifier = modifier,
            onRestart = { activeQuestions = GrammarQuizBank.buildSession(selectedType, selectedTopic) },
            onClose = { activeQuestions = null }
        )
        return
    }

    if (onClose != null) {
        BackHandler(onBack = onClose)
    }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onClose != null) {
                IconButton(onClick = onClose, modifier = Modifier.testTag("quiz_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Column {
                Text("Grammar Quiz · গ্রামার কুইজ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text("MCQ · Fill in the Blanks · Error Correction (ইংরেজি ও বাংলা ব্যাখ্যা)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(14.dp))

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Quiz, contentDescription = null, modifier = Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Quizzes completed · সম্পন্ন: ${store.quizzesCompleted}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Overall accuracy · নির্ভুলতা: ${store.accuracyPercent()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f))
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("1. Choose Quiz Mode · কুইজের ধরন বেছে নিন", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GrammarQuizMode.entries.forEach { mode ->
                val selected = selectedType == mode
                val desc = when (mode) {
                    GrammarQuizMode.MIXED -> "All question styles combined · সব ধরনের প্রশ্ন একসাথে"
                    GrammarQuizMode.MCQ -> "Pick the right option from 4 choices · ৪টি থেকে সঠিক উত্তর"
                    GrammarQuizMode.FILL -> "Type or pick the missing word · শূন্যস্থান পূরণ"
                    GrammarQuizMode.CORRECTION -> "Spot & fix the grammar error · ভুল সংশোধন"
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedType = mode }
                        .testTag("quiz_mode_${mode.name.lowercase()}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(mode.label, fontWeight = FontWeight.Bold)
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (selected) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("2. Filter Topic · বিষয় ফিল্টার করুন", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GrammarQuizBank.topics.forEach { topic ->
                FilterChip(
                    selected = selectedTopic == topic,
                    onClick = { selectedTopic = topic },
                    label = { Text("$topic · ${GrammarQuizBank.topicBn(topic)}") }
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        val previewCount = remember(selectedType, selectedTopic) {
            GrammarQuizBank.buildSession(selectedType, selectedTopic).size
        }
        Button(
            onClick = { activeQuestions = GrammarQuizBank.buildSession(selectedType, selectedTopic) },
            enabled = previewCount > 0,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("quiz_start_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Start Quiz · শুরু করুন ($previewCount)", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GrammarQuizRunner(
    title: String,
    questions: List<GrammarQuestion>,
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onRestart: () -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    var index by remember(questions) { mutableStateOf(0) }
    var score by remember(questions) { mutableStateOf(0) }
    var answered by remember(questions) { mutableStateOf(false) }
    var pickedOption by remember(questions) { mutableStateOf<String?>(null) }
    var typedAnswer by remember(questions) { mutableStateOf("") }
    var lastCorrect by remember(questions) { mutableStateOf(false) }
    var recordedFinish by remember(questions) { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (index < questions.size) {
                    Text("Score · স্কোর: $score / ${questions.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            if (index >= questions.size) {
                if (!recordedFinish) {
                    recordedFinish = true
                    store.recordQuizFinished(score, questions.size)
                }
                val pct = if (questions.isEmpty()) 0 else (score * 100) / questions.size
                Spacer(Modifier.height(16.dp))
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            when {
                                pct >= 90 -> "Outstanding! · অসাধারণ!"
                                pct >= 70 -> "Great Job! · দারুণ হয়েছে!"
                                else -> "Keep Practicing! · অনুশীলন চালিয়ে যান!"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("$score / ${questions.size} correct ($pct%)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(6.dp))
                        Text("XP and accuracy updated · XP ও নির্ভুলতা হালনাগাদ হয়েছে।", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRestart, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Text("Play Another Round · আরেকবার খেলুন")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Text("Change Mode / Topic · ধরন বা বিষয় বদলান")
                }
                return@Column
            }

            val q = questions[index]
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (index + 1f) / questions.size },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Question ${index + 1} / ${questions.size} · প্রশ্ন", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text("${q.type.label} · ${GrammarQuizBank.topicBn(q.topic)}", Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(q.prompt, modifier = Modifier.padding(18.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))

            if (q.type == GrammarQuizMode.FILL) {
                OutlinedTextField(
                    value = typedAnswer,
                    onValueChange = { if (!answered) typedAnswer = it },
                    modifier = Modifier.fillMaxWidth().testTag("quiz_fill_input"),
                    enabled = !answered,
                    singleLine = true,
                    placeholder = { Text("Type missing word or tap below / শব্দ লিখুন বা ট্যাপ করুন…") },
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(Modifier.height(10.dp))
                if (!answered) {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        q.options.forEach { hint ->
                            FilterChip(
                                selected = typedAnswer.equals(hint, ignoreCase = true),
                                onClick = { typedAnswer = hint },
                                label = { Text(hint) }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val ok = typedAnswer.trim().equals(q.answer.trim(), ignoreCase = true)
                            answered = true
                            lastCorrect = ok
                            if (ok) score += 1
                            store.recordAnswer(ok, xpOnCorrect = 7)
                        },
                        enabled = typedAnswer.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("quiz_fill_check_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Check Answer · যাচাই করুন") }
                }
            } else {
                q.options.forEach { opt ->
                    val isRight = opt == q.answer
                    val bg = when {
                        !answered -> MaterialTheme.colorScheme.surfaceVariant
                        isRight -> MaterialTheme.colorScheme.tertiaryContainer
                        opt == pickedOption -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = !answered) {
                                pickedOption = opt
                                answered = true
                                lastCorrect = isRight
                                if (isRight) score += 1
                                store.recordAnswer(isRight, xpOnCorrect = 6)
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = bg)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(opt, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            if (answered && isRight) Text("✓", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary)
                            else if (answered && opt == pickedOption) Text("✗", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (answered) {
                Spacer(Modifier.height(12.dp))
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (lastCorrect) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (lastCorrect) "Correct! · সঠিক (+XP)" else "Correct answer · সঠিক উত্তর: ${q.answer}",
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(q.explanation, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(q.explanationBn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        }
                        if (q.audioText.isNotEmpty()) {
                            Spacer(Modifier.width(8.dp))
                            FilledIconButton(
                                onClick = { speakIn(store, speaker, q.audioText, "en") },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        index += 1
                        answered = false
                        pickedOption = null
                        typedAnswer = ""
                    },
                    modifier = Modifier.fillMaxWidth().testTag("quiz_next_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (index < questions.lastIndex) "Next Question · পরবর্তী প্রশ্ন" else "View Final Score · চূড়ান্ত ফলাফল")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
