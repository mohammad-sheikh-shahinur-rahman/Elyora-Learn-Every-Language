package com.itamadersomajinc.elyora

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TenseMeta(
    val id: String,
    val name: String,
    val nameBn: String,
    val timeGroup: String,
    val timeGroupBn: String,
    val aspect: String,
    val level: String,
    val affirmative: String,
    val negative: String,
    val question: String,
    val signalWords: List<String>,
    val timelineNote: String,
    val timelineNoteBn: String,
    val tipBn: String,
    val drillQuestions: List<TenseDrillQ>
)

data class TenseDrillQ(
    val prompt: String,
    val options: List<String>,
    val answer: String,
    val explanation: String,
    val explanationBn: String,
    val modeLabel: String
)

object TenseMasteryData {
    val all: List<TenseMeta> = listOf(
        TenseMeta(
            id = "t1", name = "Present Simple", nameBn = "সাধারণ বর্তমান কাল", timeGroup = "Present", timeGroupBn = "বর্তমান", aspect = "Simple", level = "A1",
            affirmative = "Subject + V1 (he/she/it + -s/-es)",
            negative = "Subject + do/does + not + V1",
            question = "Do/Does + Subject + V1?",
            signalWords = listOf("always", "usually", "often", "every day", "never", "on Mondays"),
            timelineNote = "Facts, habits & repeating routines across all time",
            timelineNoteBn = "চিরন্তন সত্য, দৈনন্দিন অভ্যাস ও নিয়মিত কাজ বোঝাতে ব্যবহৃত হয়",
            tipBn = "টিপস: সাবজেক্ট He, She, It বা একবচন নাম হলে ভার্বের সাথে s বা es যোগ হয়।",
            drillQuestions = listOf(
                TenseDrillQ("She ___ to work by bus every morning.", listOf("go", "goes", "going", "went"), "goes", "With he/she/it in Present Simple, add -es to 'go'.", "Present Simple-এ He/She/It থাকলে 'go'-এর সাথে -es যুক্ত হয়ে 'goes' হয়।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("Choose the correct negative sentence:", listOf("He doesn't likes tea.", "He don't like tea.", "He doesn't like tea.", "He not likes tea."), "He doesn't like tea.", "After doesn't, always use the base verb without -s.", "doesn't-এর পর ভার্বের মূল রূপ বসে (-s যুক্ত হয় না)।", "Transform · রূপান্তর"),
                TenseDrillQ("___ your sister live in Dhaka?", listOf("Do", "Does", "Is", "Has"), "Does", "Use 'Does' with singular 3rd person subjects (your sister).", "তৃতীয় পুরুষ একবচন সাবজেক্ট (your sister)-এর আগে 'Does' বসে।", "Fill Verb · শূন্যস্থান")
            )
        ),
        TenseMeta(
            id = "t2", name = "Present Continuous", nameBn = "ঘটমান বর্তমান কাল", timeGroup = "Present", timeGroupBn = "বর্তমান", aspect = "Continuous", level = "A1",
            affirmative = "Subject + am/is/are + V-ing",
            negative = "Subject + am/is/are + not + V-ing",
            question = "Am/Is/Are + Subject + V-ing?",
            signalWords = listOf("now", "right now", "at the moment", "Look!", "Listen!", "currently"),
            timelineNote = "Action happening right now or temporary around now",
            timelineNoteBn = "এই মুহূর্তে চলছে এমন কাজ বা অস্থায়ী চলমান ঘটনা বোঝায়",
            tipBn = "টিপস: অনুভূতি বা মালিকানা বোঝানো ভার্বে (know, love, want) সাধারণত -ing হয় না।",
            drillQuestions = listOf(
                TenseDrillQ("Listen! Someone ___ at the door.", listOf("knocks", "is knocking", "knocked", "has knocked"), "is knocking", "'Listen!' signals an action happening right at this moment.", "'Listen!' শব্দটি ঠিক এই মুহূর্তে চলমান কাজ নির্দেশ করে।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("Which sentence is grammatically correct?", listOf("I am knowing the answer.", "She is cooking dinner now.", "They playing football.", "We are study English."), "She is cooking dinner now.", "State verbs like 'know' avoid continuous; continuous needs am/is/are + V-ing.", "'know' হলো state verb, তাই continuous হয় না; continuous-এ am/is/are + V-ing লাগে।", "Identify · শনাক্তকরণ"),
                TenseDrillQ("Turn into a question: 'They are waiting outside.'", listOf("Do they waiting outside?", "Are they waiting outside?", "Is they waiting outside?", "Waiting are they outside?"), "Are they waiting outside?", "Invert 'are' before the subject 'they'.", "সাবজেক্ট 'they'-এর আগে 'Are' বসিয়ে প্রশ্ন করতে হয়।", "Transform · রূপান্তর")
            )
        ),
        TenseMeta(
            id = "t3", name = "Present Perfect", nameBn = "পুরাঘটিত বর্তমান কাল", timeGroup = "Present", timeGroupBn = "বর্তমান", aspect = "Perfect", level = "A2",
            affirmative = "Subject + have/has + V3",
            negative = "Subject + have/has + not + V3",
            question = "Have/Has + Subject + V3?",
            signalWords = listOf("already", "just", "yet", "ever", "never", "since", "for", "recently"),
            timelineNote = "Past action connected to the present result or life experience",
            timelineNoteBn = "অতীতের কাজ যার ফলাফল এখনো বিদ্যমান, অথবা জীবনের অভিজ্ঞতা বোঝায়",
            tipBn = "টিপস: yesterday বা last year-এর মতো নির্দিষ্ট অতীত সময় উল্লেখ থাকলে Present Perfect বসাবেন না।",
            drillQuestions = listOf(
                TenseDrillQ("I ___ my keys. I can't open the door!", listOf("lose", "lost", "have lost", "am losing"), "have lost", "Present result (can't open door now) calls for Present Perfect.", "অতীতের কাজের ফলাফল এখনো বর্তমান (দরজা খুলতে পারছি না), তাই Present Perfect হবে।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("___ you ever ___ sushi?", listOf("Did / eat", "Have / eaten", "Has / eaten", "Are / eating"), "Have / eaten", "Life experiences with 'ever' use Have/Has + V3 (eaten).", "জীবনের অভিজ্ঞতা বোঝাতে 'ever'-এর সাথে Have/Has + V3 (eaten) বসে।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("Which sentence is CORRECT?", listOf("I have visited London last year.", "She has lived here since 2020.", "He have finished his work.", "They didn't finished yet."), "She has lived here since 2020.", "Use 'since + starting point' with Present Perfect; never use 'last year' with it.", "'since + শুরুর সময়' Present Perfect-এ বসে; তবে 'last year'-এর সাথে বসে না।", "Identify · শনাক্তকরণ")
            )
        ),
        TenseMeta(
            id = "t4", name = "Present Perfect Continuous", nameBn = "পুরাঘটিত ঘটমান বর্তমান কাল", timeGroup = "Present", timeGroupBn = "বর্তমান", aspect = "Perfect Continuous", level = "B1",
            affirmative = "Subject + have/has + been + V-ing",
            negative = "Subject + have/has + not + been + V-ing",
            question = "Have/Has + Subject + been + V-ing?",
            signalWords = listOf("for two hours", "since morning", "all day", "how long", "lately"),
            timelineNote = "Started in the past and continuing continuously up to now",
            timelineNoteBn = "অতীতে শুরু হয়ে এখনো একটানা চলছে এমন কাজ ও তার সময়কাল বোঝায়",
            tipBn = "টিপস: মোট সময়ের পরিমাণে 'for' (for two hours) এবং শুরুর নির্দিষ্ট সময়ে 'since' (since 8 AM) বসে।",
            drillQuestions = listOf(
                TenseDrillQ("It ___ since early this morning.", listOf("is raining", "has been raining", "rained", "was raining"), "has been raining", "'Since early this morning' emphasizes ongoing duration up to now.", "'সকাল থেকে এখনো একটানা চলছে' বোঝাতে 'has been raining' বসে।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("How long ___ you ___ English?", listOf("are / learning", "have / been learning", "did / learn", "has / been learning"), "have / been learning", "Use 'How long + have + subject + been + V-ing' for ongoing duration.", "সময়কাল জানতে 'How long + have + subject + been + V-ing' ব্যবহৃত হয়।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("Identify the tense: 'My eyes hurt because I have been reading all day.'", listOf("Present Perfect", "Past Continuous", "Present Perfect Continuous", "Present Continuous"), "Present Perfect Continuous", "have + been + reading = Present Perfect Continuous.", "have + been + V-ing হলো Present Perfect Continuous।", "Identify · শনাক্তকরণ")
            )
        ),
        TenseMeta(
            id = "t5", name = "Past Simple", nameBn = "সাধারণ অতীত কাল", timeGroup = "Past", timeGroupBn = "অতীত", aspect = "Simple", level = "A1",
            affirmative = "Subject + V2 (regular -ed / irregular)",
            negative = "Subject + did + not + V1",
            question = "Did + Subject + V1?",
            signalWords = listOf("yesterday", "last night", "two days ago", "in 2019", "then"),
            timelineNote = "Completed action at a definite finished time in the past",
            timelineNoteBn = "অতীতের কোনো নির্দিষ্ট সময়ে সম্পন্ন হয়ে যাওয়া কাজ বোঝায়",
            tipBn = "টিপস: didn't বা Did-এর পর সবসময় ভার্বের মূল রূপ (V1) বসে, কখনো V2 বসে না।",
            drillQuestions = listOf(
                TenseDrillQ("We ___ a great movie yesterday evening.", listOf("watch", "watched", "have watched", "watching"), "watched", "'Yesterday evening' is a finished past time -> Past Simple.", "'Yesterday evening' অতীতের শেষ হওয়া সময়, তাই Past Simple (watched) হবে।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("Did she ___ to the meeting?", listOf("went", "goes", "go", "going"), "go", "After 'Did', always use base form (V1), never V2.", "'Did'-এর পর সবসময় ভার্বের বেস ফর্ম (go) বসে, went নয়।", "Transform · রূপান্তর"),
                TenseDrillQ("Negative of 'He bought a new phone':", listOf("He didn't bought a new phone.", "He didn't buy a new phone.", "He wasn't buy a new phone.", "He not bought a new phone."), "He didn't buy a new phone.", "Use didn't + V1 (buy).", "না-বোধক করতে didn't + V1 (buy) ব্যবহার করতে হয়।", "Transform · রূপান্তর")
            )
        ),
        TenseMeta(
            id = "t6", name = "Past Continuous", nameBn = "ঘটমান অতীত কাল", timeGroup = "Past", timeGroupBn = "অতীত", aspect = "Continuous", level = "A2",
            affirmative = "Subject + was/were + V-ing",
            negative = "Subject + was/were + not + V-ing",
            question = "Was/Were + Subject + V-ing?",
            signalWords = listOf("while", "when", "at 8 PM yesterday", "at that moment", "all afternoon"),
            timelineNote = "Background action in progress at a past moment (often interrupted)",
            timelineNoteBn = "অতীতে কোনো নির্দিষ্ট সময়ে চলমান ছিল এমন কাজ (বা অন্য ঘটনায় বাধাপ্রাপ্ত কাজ) বোঝায়",
            tipBn = "টিপস: চলমান কাজের মাঝে হঠাৎ কিছু ঘটলে চলমানটিতে Past Continuous এবং হঠাৎ কাজটিতে Past Simple বসে।",
            drillQuestions = listOf(
                TenseDrillQ("I ___ dinner when the phone rang.", listOf("cooked", "was cooking", "am cooking", "have cooked"), "was cooking", "Longer background action (was cooking) interrupted by Past Simple (rang).", "দীর্ঘ চলমান কাজ (was cooking) মাঝপথে Past Simple (rang) দ্বারা থেমেছে।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("While we ___ in the park, it started to rain.", listOf("walked", "were walking", "was walking", "are walking"), "were walking", "Plural subject 'we' takes 'were + V-ing'.", "বহুবচন সাবজেক্ট 'we'-এর সাথে 'were + V-ing' বসে।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("Make a question: 'He was sleeping at ten.'", listOf("Did he sleeping at ten?", "Was he sleeping at ten?", "Were he sleeping at ten?", "Is he sleeping at ten?"), "Was he sleeping at ten?", "Invert 'Was' before 'he'.", "সাবজেক্ট 'he'-এর আগে 'Was' বসিয়ে প্রশ্ন তৈরি করুন।", "Transform · রূপান্তর")
            )
        ),
        TenseMeta(
            id = "t7", name = "Past Perfect", nameBn = "পুরাঘটিত অতীত কাল", timeGroup = "Past", timeGroupBn = "অতীত", aspect = "Perfect", level = "B1",
            affirmative = "Subject + had + V3",
            negative = "Subject + had + not + V3",
            question = "Had + Subject + V3?",
            signalWords = listOf("before", "after", "already", "by the time", "just", "until"),
            timelineNote = "Earlier past action completed BEFORE another past event",
            timelineNoteBn = "অতীতে দুটি কাজের মধ্যে যে কাজটি তুলনামূলক আগে শেষ হয়েছিল তা বোঝায়",
            tipBn = "টিপস: আগে ঘটা কাজটিতে had + V3 (Past Perfect) এবং পরের কাজটিতে V2 (Past Simple) বসে।",
            drillQuestions = listOf(
                TenseDrillQ("When we arrived at the station, the train ___ already ___.", listOf("has / left", "had / left", "was / leaving", "did / leave"), "had / left", "Train leaving happened BEFORE we arrived -> Past Perfect (had + V3).", "আমরা পৌঁছানোর আগেই ট্রেন ছেড়ে গিয়েছিল, তাই Past Perfect (had + left) হবে।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("She told me she ___ never ___ snow before.", listOf("has / seen", "had / seen", "was / seeing", "did / saw"), "had / seen", "Earlier past experience before she spoke -> had + seen.", "সে বলার আগের অভিজ্ঞতা বোঝাতে had + seen ব্যবহৃত হয়।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("Which action happened FIRST in: 'After he had finished work, he went home.'?", listOf("He went home", "He finished work", "Both at once", "Neither"), "He finished work", "Past Perfect (had finished) always marks the earlier past event.", "Past Perfect (had finished) সবসময় আগে ঘটা কাজটিকে নির্দেশ করে।", "Identify · শনাক্তকরণ")
            )
        ),
        TenseMeta(
            id = "t8", name = "Past Perfect Continuous", nameBn = "পুরাঘটিত ঘটমান অতীত কাল", timeGroup = "Past", timeGroupBn = "অতীত", aspect = "Perfect Continuous", level = "B2",
            affirmative = "Subject + had + been + V-ing",
            negative = "Subject + had + not + been + V-ing",
            question = "Had + Subject + been + V-ing?",
            signalWords = listOf("for hours before", "since morning", "all day before", "how long ... before"),
            timelineNote = "Ongoing duration in the past right up until another past moment",
            timelineNoteBn = "অতীতের আরেকটি ঘটনার আগ পর্যন্ত কোনো কাজ কতক্ষণ ধরে একটানা চলছিল তা বোঝায়",
            tipBn = "টিপস: অতীতে কোনো ঘটনার আগে দীর্ঘ সময় ধরে চলমান প্রচেষ্টা বোঝাতে had been + V-ing ব্যবহার করুন।",
            drillQuestions = listOf(
                TenseDrillQ("They were exhausted because they ___ for six hours.", listOf("drove", "had been driving", "have been driving", "are driving"), "had been driving", "Continuous effort in the past leading up to a past state (were exhausted).", "অতীতের অবস্থার (were exhausted) আগ পর্যন্ত ছয় ঘণ্টা ধরে চলা কাজ -> had been driving।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("She ___ at that company for five years before it closed.", listOf("had been working", "has been working", "is working", "works"), "had been working", "Duration before a past point ('before it closed') -> had been + V-ing.", "অতীতের একটি বিন্দুর ('before it closed') আগের সময়কাল বোঝাতে had been + V-ing।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("Identify the tense: 'He had been waiting for an hour when she arrived.'", listOf("Past Perfect", "Past Continuous", "Past Perfect Continuous", "Present Perfect Continuous"), "Past Perfect Continuous", "had + been + waiting = Past Perfect Continuous.", "had + been + waiting হলো Past Perfect Continuous।", "Identify · শনাক্তকরণ")
            )
        ),
        TenseMeta(
            id = "t9", name = "Future Simple", nameBn = "সাধারণ ভবিষ্যৎ কাল", timeGroup = "Future", timeGroupBn = "ভবিষ্যৎ", aspect = "Simple", level = "A1",
            affirmative = "Subject + will + V1",
            negative = "Subject + will not (won't) + V1",
            question = "Will + Subject + V1?",
            signalWords = listOf("tomorrow", "next week", "soon", "I think", "probably", "promise"),
            timelineNote = "Instant decisions, promises, offers & predictions about the future",
            timelineNoteBn = "ভবিষ্যতের তাৎক্ষণিক সিদ্ধান্ত, প্রতিশ্রুতি, প্রস্তাব ও অনুমান বোঝায়",
            tipBn = "টিপস: কথার মুহূর্তে নেওয়া সিদ্ধান্ত ও প্রতিশ্রুতিতে 'will + V1' বসে।",
            drillQuestions = listOf(
                TenseDrillQ("Don't worry, I ___ you carry those heavy bags.", listOf("help", "will help", "helped", "helping"), "will help", "Spontaneous offer/promise made at the moment of speaking -> will + V1.", "তাৎক্ষণিক সাহায্যের প্রস্তাব বা প্রতিশ্রুতি বোঝাতে 'will + V1' বসে।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("Choose the correct negative form:", listOf("She won't to come.", "She willn't come.", "She won't come.", "She doesn't will come."), "She won't come.", "Contraction of 'will not' is 'won't' + base verb (no 'to').", "'will not'-এর সংক্ষিপ্ত রূপ 'won't' এবং এরপর 'to' ছাড়াই V1 বসে।", "Transform · রূপান্তর"),
                TenseDrillQ("I think it ___ tomorrow afternoon.", listOf("rains", "will rain", "rained", "has rained"), "will rain", "Predictions with 'I think' use will + base verb.", "'I think' দিয়ে ভবিষ্যতের অনুমান করলে will + V1 বসে।", "Fill Verb · শূন্যস্থান")
            )
        ),
        TenseMeta(
            id = "t10", name = "Future Continuous", nameBn = "ঘটমান ভবিষ্যৎ কাল", timeGroup = "Future", timeGroupBn = "ভবিষ্যৎ", aspect = "Continuous", level = "B1",
            affirmative = "Subject + will + be + V-ing",
            negative = "Subject + won't + be + V-ing",
            question = "Will + Subject + be + V-ing?",
            signalWords = listOf("this time tomorrow", "at 9 PM tonight", "while you are", "in an hour"),
            timelineNote = "Action that will be in progress at a specific moment in the future",
            timelineNoteBn = "ভবিষ্যতের কোনো নির্দিষ্ট মুহূর্তে যে কাজটি চলতে থাকবে তা বোঝায়",
            tipBn = "টিপস: আগামীকাল বা ভবিষ্যতের কোনো নির্দিষ্ট সময়ে চলমান কাজ বোঝাতে will be + V-ing ব্যবহার করুন।",
            drillQuestions = listOf(
                TenseDrillQ("This time tomorrow, we ___ over the ocean.", listOf("fly", "will fly", "will be flying", "have flown"), "will be flying", "'This time tomorrow' points to an action in progress at a future moment.", "'আগামীকাল ঠিক এই সময়ে' চলমান কাজ বোঝাতে 'will be flying' বসে।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("Don't call at 8 PM; I ___ dinner with my family.", listOf("will be having", "had", "have had", "am had"), "will be having", "Action in progress at 8 PM tonight -> will be + V-ing.", "রাত ৮টায় চলমান থাকবে এমন কাজ -> will be + V-ing।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("Identify the tense: 'Will you be using the laptop tonight?'", listOf("Future Simple", "Future Continuous", "Future Perfect", "Present Continuous"), "Future Continuous", "Will + subject + be + V-ing = Future Continuous (also polite inquiry).", "Will + subject + be + V-ing হলো Future Continuous।", "Identify · শনাক্তকরণ")
            )
        ),
        TenseMeta(
            id = "t11", name = "Future Perfect", nameBn = "পুরাঘটিত ভবিষ্যৎ কাল", timeGroup = "Future", timeGroupBn = "ভবিষ্যৎ", aspect = "Perfect", level = "B2",
            affirmative = "Subject + will + have + V3",
            negative = "Subject + won't + have + V3",
            question = "Will + Subject + have + V3?",
            signalWords = listOf("by tomorrow", "by next year", "by the time", "before Friday"),
            timelineNote = "Action that will be COMPLETED before a specific deadline in the future",
            timelineNoteBn = "ভবিষ্যতের কোনো নির্দিষ্ট সময়ের আগেই কাজটি শেষ হয়ে থাকবে বোঝায়",
            tipBn = "টিপস: 'by tomorrow / by Friday' ধরনের সময়সীমা থাকলে সাধারণত will have + V3 বসে।",
            drillQuestions = listOf(
                TenseDrillQ("By next Friday, I ___ this entire report.", listOf("finish", "will finish", "will have finished", "am finishing"), "will have finished", "'By next Friday' indicates completion before a future deadline -> will have + V3.", "'আগামী শুক্রবারের মধ্যে' কাজ শেষ হবে বোঝাতে 'will have finished' বসে।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("By the time you arrive, they ___ already ___.", listOf("will / have left", "will / leaving", "have / left", "had / left"), "will / have left", "Use Future Perfect (will have + V3) with 'by the time + present simple'.", "'by the time + present simple'-এর সাথে Future Perfect (will have + V3) বসে।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("Select the correct structure for Future Perfect:", listOf("will + V3", "will + be + V-ing", "will + have + V3", "will + have + been + V-ing"), "will + have + V3", "Future Perfect always follows Subject + will + have + past participle (V3).", "Future Perfect-এর সঠিক গঠন হলো Subject + will + have + V3।", "Identify · শনাক্তকরণ")
            )
        ),
        TenseMeta(
            id = "t12", name = "Future Perfect Continuous", nameBn = "পুরাঘটিত ঘটমান ভবিষ্যৎ কাল", timeGroup = "Future", timeGroupBn = "ভবিষ্যৎ", aspect = "Perfect Continuous", level = "C1",
            affirmative = "Subject + will + have + been + V-ing",
            negative = "Subject + won't + have + been + V-ing",
            question = "Will + Subject + have + been + V-ing?",
            signalWords = listOf("by next month ... for 5 years", "for two hours by noon", "by the end of the year"),
            timelineNote = "Emphasizes how long an action will have been continuing by a future point",
            timelineNoteBn = "ভবিষ্যতের কোনো নির্দিষ্ট সময় পর্যন্ত একটি কাজ কতক্ষণ ধরে চলতে থাকবে তা বোঝায়",
            tipBn = "টিপস: ভবিষ্যতের শেষ সীমা (By...) এবং সময়ের ব্যাপ্তি (for...) একসাথে থাকলে এটি ব্যবহৃত হয়।",
            drillQuestions = listOf(
                TenseDrillQ("By December, she ___ here for ten years.", listOf("will work", "will be working", "will have been working", "has been working"), "will have been working", "Future point ('By December') + duration ('for ten years') -> will have been + V-ing.", "ভবিষ্যৎ বিন্দু ('By December') + সময়কাল ('for ten years') -> will have been working।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("At 6 PM, we ___ for over four hours straight.", listOf("will have been studying", "will study", "studied", "are studying"), "will have been studying", "Focuses on ongoing duration up to a future clock time.", "ভবিষ্যতের নির্দিষ্ট সময় পর্যন্ত একটানা চলমান কাজের ব্যাপ্তি বোঝায়।", "Fill Verb · শূন্যস্থান"),
                TenseDrillQ("Identify the tense: 'By next year, they will have been living abroad for a decade.'", listOf("Future Perfect", "Future Continuous", "Future Perfect Continuous", "Present Perfect Continuous"), "Future Perfect Continuous", "will + have + been + V-ing = Future Perfect Continuous.", "will + have + been + V-ing হলো Future Perfect Continuous।", "Identify · শনাক্তকরণ")
            )
        ),
        TenseMeta(
            id = "t13", name = "Future Plans (Be Going To)", nameBn = "পরিকল্পিত ভবিষ্যৎ (Be Going To)", timeGroup = "Future", timeGroupBn = "ভবিষ্যৎ", aspect = "Planned Future", level = "A1",
            affirmative = "Subject + am/is/are + going to + V1",
            negative = "Subject + am/is/are + not + going to + V1",
            question = "Am/Is/Are + Subject + going to + V1?",
            signalWords = listOf("tomorrow", "next weekend", "soon", "I've decided", "look at those clouds!"),
            timelineNote = "Pre-decided plans & intentions, plus predictions with present evidence",
            timelineNoteBn = "আগে থেকে ঠিক করা ভবিষ্যৎ পরিকল্পনা বা বর্তমান প্রমাণ দেখে অনুমান বোঝাতে ব্যবহৃত হয়",
            tipBn = "টিপস: তাৎক্ষণিক সিদ্ধান্তে 'will', আর আগে থেকে ঠিক করা পরিকল্পনায় 'going to' বসে।",
            drillQuestions = listOf(
                TenseDrillQ("Look at those dark clouds! It ___ rain soon.", listOf("will", "is going to", "going to", "rained"), "is going to", "Evidence visible right now ('dark clouds') -> use 'is going to'.", "এখনই প্রমাণ দেখা যাচ্ছে ('কালো মেঘ') -> 'is going to' হবে।", "Evidence · প্রমাণ"),
                TenseDrillQ("We have already bought tickets. We ___ visit Cox's Bazar next week.", listOf("are going to", "going to", "will have", "were"), "are going to", "A pre-decided plan with tickets already bought uses 'am/is/are going to'.", "আগে থেকেই টিকিট কেনা ও পরিকল্পনা করা থাকলে 'are going to' বসে।", "Plan · পরিকল্পনা"),
                TenseDrillQ("Choose the correct negative with going to:", listOf("They not going to come.", "They aren't going to come.", "They don't going to come.", "They isn't going to come."), "They aren't going to come.", "Negative is am/is/are + not + going to + base verb.", "না-বোধক রূপ: Subject + am/is/are + not + going to + V1।", "Transform · রূপান্তর")
            )
        )
    )
}

@Composable
fun TenseMasteryScreen(
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    var selectedGroup by remember { mutableStateOf("All") }
    var openTense by remember { mutableStateOf<TenseMeta?>(null) }
    var mixedDrill by remember { mutableStateOf(false) }

    if (mixedDrill) {
        TenseDrillView(
            title = "13 Tenses Mixed Practice · ১৩ টেন্স অনুশীলন",
            questions = remember { TenseMasteryData.all.flatMap { it.drillQuestions }.shuffled().take(13) },
            tenseIdToMaster = null,
            store = store,
            speaker = speaker,
            modifier = modifier,
            onClose = { mixedDrill = false }
        )
        return
    }

    if (openTense != null) {
        TenseDetailView(
            meta = openTense!!,
            store = store,
            speaker = speaker,
            modifier = modifier,
            onClose = { openTense = null }
        )
        return
    }

    if (onClose != null) {
        BackHandler(onBack = onClose)
    }

    val groups = listOf("All" to "All · সব", "Present" to "Present · বর্তমান", "Past" to "Past · অতীত", "Future" to "Future · ভবিষ্যৎ")
    val filtered = remember(selectedGroup) {
        if (selectedGroup == "All") TenseMasteryData.all
        else TenseMasteryData.all.filter { it.timeGroup == selectedGroup }
    }
    val masteredCount = TenseMasteryData.all.count { it.id in store.masteredTenses }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onClose != null) {
                IconButton(onClick = onClose, modifier = Modifier.testTag("tense_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Column(Modifier.weight(1f)) {
                Text("Tense Mastery · টেন্স মাস্টারি", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text(
                    "All 13 Tenses (incl. Be Going To) · ১৩টি টেন্সের গঠন, বাংলা ব্যাখ্যা ও অনুশীলন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                val totalTenses = TenseMasteryData.all.size.coerceAtLeast(13)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$masteredCount / $totalTenses Mastered · ${totalTenses}টির মধ্যে ${masteredCount}টি সম্পন্ন",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${(masteredCount * 100) / totalTenses}%", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { masteredCount / totalTenses.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { mixedDrill = true },
                    modifier = Modifier.fillMaxWidth().testTag("tense_mixed_drill_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Quiz, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Practice All 13 Tenses · ১৩টি টেন্স অনুশীলন করুন")
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groups.forEach { (key, label) ->
                FilterChip(
                    selected = selectedGroup == key,
                    onClick = { selectedGroup = key },
                    label = { Text(label) },
                    modifier = Modifier.testTag("tense_filter_${key.lowercase()}")
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { t ->
                val mastered = t.id in store.masteredTenses
                val groupColor = when (t.timeGroup) {
                    "Present" -> MaterialTheme.colorScheme.primaryContainer
                    "Past" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openTense = t }
                        .testTag("tense_card_${t.id}"),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(groupColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                t.timeGroup.take(2).uppercase(),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(t.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(t.level, Modifier.padding(horizontal = 6.dp, vertical = 1.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                if (mastered) {
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "Mastered", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(t.nameBn, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text(t.affirmative, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                            Text(t.timelineNoteBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Open tense", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TenseDetailView(
    meta: TenseMeta,
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    var startDrill by remember { mutableStateOf(false) }
    if (startDrill) {
        TenseDrillView(
            title = "${meta.name} · ${meta.nameBn}",
            questions = meta.drillQuestions,
            tenseIdToMaster = meta.id,
            store = store,
            speaker = speaker,
            modifier = modifier,
            onClose = { startDrill = false }
        )
        return
    }
    BackHandler(onBack = onClose)
    val pattern = remember(meta.id) { Tenses.patterns.firstOrNull { it.id == meta.id } }
    val examples = remember(pattern) { pattern?.examples ?: emptyList() }
    val mastered = meta.id in store.masteredTenses

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("tense_detail_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(Modifier.weight(1f)) {
                Text("${meta.timeGroup} (${meta.timeGroupBn}) · ${meta.aspect} · ${meta.level}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(meta.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(meta.nameBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
            }
            if (mastered) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Mastered", tint = MaterialTheme.colorScheme.tertiary)
            }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(10.dp))
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("STRUCTURES · গঠন প্রণালী", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    FormulaRow("Affirmative (+) · হ্যাঁ-বোধক", meta.affirmative)
                    FormulaRow("Negative (−) · না-বোধক", meta.negative)
                    FormulaRow("Question (?) · প্রশ্নবোধক", meta.question)
                }
            }

            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("WHEN TO USE IT · কখন ব্যবহার করবেন", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(meta.timelineNote, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(meta.timelineNoteBn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                    if (pattern != null) {
                        val tipLine = pattern.ruleEn.lines().firstOrNull { it.startsWith("TIP:") }
                        if (!tipLine.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(tipLine, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(meta.tipBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("SIGNAL WORDS · চেনার উপায় (ইঙ্গিত শব্দ)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        meta.signalWords.forEach { w ->
                            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text(
                                    w,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { startDrill = true },
                modifier = Modifier.fillMaxWidth().testTag("tense_start_drill_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Quiz, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (mastered) "Practice Again · আবার অনুশীলন (+10 XP)" else "Start Exercise · অনুশীলন শুরু (+25 XP)")
            }

            Spacer(Modifier.height(16.dp))
            Text("Audio Examples (${examples.size}) · অডিও উদাহরণ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            examples.forEach { ex ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(14.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(ex.ja, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                            val tr = ex.glossFor(store.nativeLang, "en")
                            if (tr.isNotEmpty()) {
                                Spacer(Modifier.height(2.dp))
                                Text(tr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        FilledIconButton(
                            onClick = { speakIn(store, speaker, ex.ja, "en") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormulaRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TenseDrillView(
    title: String,
    questions: List<TenseDrillQ>,
    tenseIdToMaster: String?,
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    var index by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var picked by remember { mutableStateOf<String?>(null) }
    var savedMastery by remember { mutableStateOf(false) }

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
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            if (index >= questions.size) {
                if (!savedMastery) {
                    savedMastery = true
                    if (tenseIdToMaster != null && score >= (questions.size * 2) / 3) {
                        store.markTenseMastered(tenseIdToMaster)
                    } else {
                        store.recordQuizFinished(score, questions.size)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (score == questions.size) "Tense Mastered! · টেন্স সম্পন্ন!" else "Practice Complete · অনুশীলন সম্পন্ন",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("Score · স্কোর: $score / ${questions.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { index = 0; score = 0; picked = null; savedMastery = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Retry Exercise · আবার চেষ্টা করুন") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Back to Tenses · টেন্স তালিকায় ফিরুন") }
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
                Text("Question ${index + 1} / ${questions.size} · প্রশ্ন", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(q.modeLabel, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    q.prompt,
                    modifier = Modifier.padding(18.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(14.dp))

            val revealed = picked != null
            q.options.forEach { opt ->
                val isCorrect = opt == q.answer
                val bg = when {
                    !revealed -> MaterialTheme.colorScheme.surfaceVariant
                    isCorrect -> MaterialTheme.colorScheme.tertiaryContainer
                    opt == picked -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !revealed) {
                            picked = opt
                            if (isCorrect) score += 1
                            store.recordAnswer(isCorrect, xpOnCorrect = 6)
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = bg)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(opt, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        if (revealed && isCorrect) Text("✓", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary)
                        else if (revealed && opt == picked) Text("✗", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (revealed) {
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Explanation · ব্যাখ্যা", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(q.explanation, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(q.explanationBn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { index += 1; picked = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (index < questions.lastIndex) "Next Question · পরবর্তী প্রশ্ন" else "See Results · ফলাফল দেখুন")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
