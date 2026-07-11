package com.biblereadingpath.app.data.studyplan

import com.biblereadingpath.app.data.BibleBooks

object TopicPlans {

    val ALL = listOf(
        PlanDefinition(
            id = "faith",
            name = "Faith",
            description = "Explore what it means to trust God through 7 key chapters",
            icon = "\uD83D\uDD25",
            typeKey = "topic",
            chapters = listOf(
                PlanChapter("Hebrews", 11, "The Hall of Faith"),
                PlanChapter("Romans", 4, "Abraham's Faith"),
                PlanChapter("Habakkuk", 2, "The Righteous Shall Live by Faith"),
                PlanChapter("James", 1, "Testing of Your Faith"),
                PlanChapter("Matthew", 17, "Faith as a Mustard Seed"),
                PlanChapter("Mark", 9, "Lord, I Believe, Help My Unbelief"),
                PlanChapter("Hebrews", 12, "Fixing Our Eyes on Jesus")
            )
        ),
        PlanDefinition(
            id = "anxiety",
            name = "Peace Over Anxiety",
            description = "Find calm and trust in God through 7 comforting chapters",
            icon = "\uD83C\uDF3F",
            typeKey = "topic",
            chapters = listOf(
                PlanChapter("Philippians", 4, "Do Not Be Anxious"),
                PlanChapter("Matthew", 6, "Do Not Worry"),
                PlanChapter("Psalm", 23, "The Lord Is My Shepherd"),
                PlanChapter("Isaiah", 41, "Fear Not, For I Am With You"),
                PlanChapter("1 Peter", 5, "Cast Your Anxiety on Him"),
                PlanChapter("Psalm", 46, "God Is Our Refuge"),
                PlanChapter("John", 14, "Peace I Leave With You")
            )
        ),
        PlanDefinition(
            id = "forgiveness",
            name = "Forgiveness",
            description = "Discover the power and freedom of forgiveness in 7 chapters",
            icon = "\uD83E\uDEDA",
            typeKey = "topic",
            chapters = listOf(
                PlanChapter("Matthew", 18, "Forgive Seventy-Seven Times"),
                PlanChapter("Colossians", 3, "Forgive as the Lord Forgave You"),
                PlanChapter("Ephesians", 4, "Be Kind and Compassionate"),
                PlanChapter("Luke", 15, "The Prodigal Son"),
                PlanChapter("Genesis", 50, "Joseph Forgives His Brothers"),
                PlanChapter("Psalm", 51, "Create in Me a Clean Heart"),
                PlanChapter("1 John", 1, "If We Confess Our Sins")
            )
        ),
        PlanDefinition(
            id = "gratitude",
            name = "Gratitude",
            description = "Cultivate a thankful heart through 7 uplifting chapters",
            icon = "\uD83C\uDF38",
            typeKey = "topic",
            chapters = listOf(
                PlanChapter("1 Thessalonians", 5, "Give Thanks in All Circumstances"),
                PlanChapter("Psalm", 100, "Enter His Gates with Thanksgiving"),
                PlanChapter("Colossians", 3, "Be Thankful"),
                PlanChapter("Philippians", 4, "With Thanksgiving Present Your Requests"),
                PlanChapter("Psalm", 107, "Give Thanks to the Lord"),
                PlanChapter("James", 1, "Every Good and Perfect Gift"),
                PlanChapter("Psalm", 136, "His Love Endures Forever")
            )
        ),
        PlanDefinition(
            id = "love",
            name = "Love",
            description = "Understand God's love and how to love others in 7 chapters",
            icon = "\u2764\uFE0F",
            typeKey = "topic",
            chapters = listOf(
                PlanChapter("1 Corinthians", 13, "The Way of Love"),
                PlanChapter("1 John", 4, "God Is Love"),
                PlanChapter("Romans", 8, "Nothing Can Separate Us"),
                PlanChapter("John", 15, "Greater Love Has No One"),
                PlanChapter("John", 3, "For God So Loved the World"),
                PlanChapter("1 John", 3, "Lay Down Our Lives"),
                PlanChapter("Song of Solomon", 8, "Love Is as Strong as Death")
            )
        ),
        PlanDefinition(
            id = "prayer",
            name = "Prayer",
            description = "Deepen your prayer life through 7 essential chapters",
            icon = "\uD83D\uDD4C",
            typeKey = "topic",
            chapters = listOf(
                PlanChapter("Matthew", 6, "The Lord's Prayer"),
                PlanChapter("James", 5, "The Prayer of a Righteous Person"),
                PlanChapter("Psalm", 63, "My Soul Thirsts for You"),
                PlanChapter("1 Timothy", 2, "Prayers for Everyone"),
                PlanChapter("Luke", 11, "Ask, Seek, Knock"),
                PlanChapter("Psalm", 145, "I Will Exalt You"),
                PlanChapter("Romans", 8, "The Spirit Intercedes")
            )
        ),
        PlanDefinition(
            id = "wisdom",
            name = "Wisdom",
            description = "Gain practical wisdom for daily life in 7 chapters",
            icon = "\uD83D\uDCDC",
            typeKey = "topic",
            chapters = listOf(
                PlanChapter("Proverbs", 3, "Trust in the Lord"),
                PlanChapter("James", 1, "Ask God for Wisdom"),
                PlanChapter("Proverbs", 4, "Get Wisdom"),
                PlanChapter("Ecclesiastes", 3, "A Time for Everything"),
                PlanChapter("Proverbs", 16, "Commit to the Lord"),
                PlanChapter("Colossians", 4, "Walk in Wisdom"),
                PlanChapter("Proverbs", 9, "Wisdom Has Built Her House")
            )
        )
    )

    fun getById(id: String): PlanDefinition? = ALL.find { it.id == id }
}
