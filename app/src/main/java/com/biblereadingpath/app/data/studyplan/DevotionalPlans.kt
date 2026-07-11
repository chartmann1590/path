package com.biblereadingpath.app.data.studyplan

object DevotionalPlans {

    val ALL = listOf(
        PlanDefinition(
            id = "foundations_7",
            name = "Foundations",
            description = "A 7-day journey through the foundational passages of faith",
            icon = "\uD83D\uDD3B",
            typeKey = "devotional",
            chapters = listOf(
                PlanChapter("Genesis", 1, "In the Beginning"),
                PlanChapter("Psalm", 19, "The heavens declare"),
                PlanChapter("Isaiah", 53, "The Suffering Servant"),
                PlanChapter("Matthew", 5, "The Beatitudes"),
                PlanChapter("John", 1, "The Word Became Flesh"),
                PlanChapter("Romans", 8, "More Than Conquerors"),
                PlanChapter("Revelation", 21, "All Things New")
            ),
            reflectionPrompts = mapOf(
                "Genesis-1" to "What does it mean to you that God created everything and called it good? How does this shape your view of the world?",
                "Psalm-19" to "Where do you see God's glory revealed in creation around you? Take a moment to notice and give thanks.",
                "Isaiah-53" to "Reflect on the depth of God's love shown through the suffering servant. What does this sacrifice mean for your daily life?",
                "Matthew-5" to "Which beatitude speaks most to your current season? How can you live it out today?",
                "John-1" to "The Word became flesh and dwelt among us. How does knowing God came near change how you approach Him?",
                "Romans-8" to "What does it mean to be more than a conqueror through Christ? Where do you need this truth today?",
                "Revelation-21" to "How does the promise of all things new give you hope? Carry this hope into tomorrow."
            )
        ),
        PlanDefinition(
            id = "jesus_teachings_7",
            name = "Jesus' Teachings",
            description = "Walk through 7 of Jesus' most powerful teachings",
            icon = "\u2726",
            typeKey = "devotional",
            chapters = listOf(
                PlanChapter("Matthew", 5, "The Sermon on the Mount"),
                PlanChapter("Matthew", 6, "Prayer and Trust"),
                PlanChapter("Matthew", 13, "The Parables of the Kingdom"),
                PlanChapter("Luke", 10, "The Good Samaritan"),
                PlanChapter("Luke", 15, "The Lost Sheep and Prodigal Son"),
                PlanChapter("John", 10, "The Good Shepherd"),
                PlanChapter("John", 15, "The Vine and the Branches")
            ),
            reflectionPrompts = mapOf(
                "Matthew-5" to "Jesus flips our expectations upside down. Which teaching challenges you the most today?",
                "Matthew-6" to "Jesus teaches us to pray simply and trust deeply. What worry can you release to God right now?",
                "Matthew-13" to "The kingdom of heaven is like a tiny seed that grows. What small seed is God growing in your life?",
                "Luke-10" to "Who is your neighbor? Is there someone God is calling you to show compassion to this week?",
                "Luke-15" to "Both the lost sheep and the prodigal son show God's relentless pursuit. How does it feel to be sought after?",
                "John-10" to "Jesus says His sheep know His voice. How do you discern His voice amid the noise of life?",
                "John-15" to "Apart from Him we can do nothing. What does abiding in the vine look like in your daily routine?"
            )
        ),
        PlanDefinition(
            id = "psalms_comfort_7",
            name = "Psalms of Comfort",
            description = "Find peace and refuge in 7 beloved Psalms",
            icon = "\uD83C\uDF40",
            typeKey = "devotional",
            chapters = listOf(
                PlanChapter("Psalm", 23, "The Lord Is My Shepherd"),
                PlanChapter("Psalm", 27, "The Lord Is My Light"),
                PlanChapter("Psalm", 34, "Taste and See"),
                PlanChapter("Psalm", 42, "As the Deer Pants"),
                PlanChapter("Psalm", 46, "God Is Our Refuge"),
                PlanChapter("Psalm", 91, "Under His Wings"),
                PlanChapter("Psalm", 121, "My Help Comes from the Lord")
            ),
            reflectionPrompts = mapOf(
                "Psalm-23" to "David wrote this in dark valleys. What valley are you walking through, and how can the Shepherd guide you?",
                "Psalm-27" to "What does it mean to seek God's face? Spend a moment simply being present with Him.",
                "Psalm-34" to "The Lord is close to the brokenhearted. Where do you need His nearness today?",
                "Psalm-42" to "The psalmist is honest about his thirst for God. What are you longing for? Bring it to Him.",
                "Psalm-46" to "Be still and know that He is God. Practice stillness for a few moments and listen.",
                "Psalm-91" to "Under His wings you will find refuge. What do you need protection from? Ask Him to shelter you.",
                "Psalm-121" to "Your help comes from the Maker of heaven and earth. Release a burden to Him right now."
            )
        ),
        PlanDefinition(
            id = "new_believer_30",
            name = "30-Day New Believer",
            description = "A month-long journey through the essentials of the Christian faith",
            icon = "\uD83C\uDF1F",
            typeKey = "devotional",
            chapters = listOf(
                PlanChapter("Genesis", 1, "God the Creator"),
                PlanChapter("Genesis", 3, "The Fall"),
                PlanChapter("Genesis", 12, "God's Promise to Abraham"),
                PlanChapter("Exodus", 20, "The Ten Commandments"),
                PlanChapter("Deuteronomy", 6, "Love the Lord Your God"),
                PlanChapter("Psalm", 23, "The Lord Is My Shepherd"),
                PlanChapter("Psalm", 51, "Repentance and Grace"),
                PlanChapter("Isaiah", 9, "A Son Is Given"),
                PlanChapter("Isaiah", 53, "The Suffering Servant"),
                PlanChapter("Matthew", 1, "The Birth of Jesus"),
                PlanChapter("Matthew", 5, "The Beatitudes"),
                PlanChapter("Matthew", 6, "The Lord's Prayer"),
                PlanChapter("Mark", 2, "Jesus Forgives and Heals"),
                PlanChapter("Luke", 15, "Lost and Found"),
                PlanChapter("Luke", 23, "The Crucifixion"),
                PlanChapter("Luke", 24, "The Resurrection"),
                PlanChapter("John", 1, "The Word Became Flesh"),
                PlanChapter("John", 3, "Born Again"),
                PlanChapter("John", 14, "The Way, the Truth, the Life"),
                PlanChapter("John", 15, "Abiding in Christ"),
                PlanChapter("Acts", 2, "The Holy Spirit Comes"),
                PlanChapter("Acts", 9, "Paul's Conversion"),
                PlanChapter("Romans", 3, "All Have Sinned"),
                PlanChapter("Romans", 5, "Justified by Faith"),
                PlanChapter("Romans", 8, "More Than Conquerors"),
                PlanChapter("Romans", 12, "Living Sacrifices"),
                PlanChapter("1 Corinthians", 13, "The Way of Love"),
                PlanChapter("Galatians", 5, "Fruit of the Spirit"),
                PlanChapter("Ephesians", 2, "Saved by Grace"),
                PlanChapter("Revelation", 21, "All Things New")
            ),
            reflectionPrompts = mapOf(
                "Genesis-1" to "God created everything with purpose and called it good. You are part of that creation. How does knowing you were made on purpose change how you see yourself?",
                "Ephesians-2" to "You have been saved by grace through faith, not by works. Let this truth sink in deeply today."
            )
        )
    )

    fun getById(id: String): PlanDefinition? = ALL.find { it.id == id }
}
