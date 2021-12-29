package io.heckel.ntfy.util

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.Window
import io.heckel.ntfy.data.Notification
import io.heckel.ntfy.data.Subscription
import java.text.DateFormat
import java.util.*

fun topicUrl(baseUrl: String, topic: String) = "${baseUrl}/${topic}"
fun topicUrlUp(baseUrl: String, topic: String) = "${baseUrl}/${topic}?up=1" // UnifiedPush
fun topicUrlJson(baseUrl: String, topic: String, since: String) = "${topicUrl(baseUrl, topic)}/json?since=$since"
fun topicUrlJsonPoll(baseUrl: String, topic: String) = "${topicUrl(baseUrl, topic)}/json?poll=1"
fun topicShortUrl(baseUrl: String, topic: String) =
    topicUrl(baseUrl, topic)
        .replace("http://", "")
        .replace("https://", "")

fun formatDateShort(timestampSecs: Long): String {
    val mutedUntilDate = Date(timestampSecs*1000)
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(mutedUntilDate)
}

fun toPriority(priority: Int?): Int {
    if (priority != null && (1..5).contains(priority)) return priority
    else return 3
}

fun joinTags(tags: List<String>?): String {
    return tags?.joinToString(",") ?: ""
}

fun joinTagsMap(tags: List<String>?): String {
    return tags?.mapIndexed { i, tag -> "${i+1}=${tag}" }?.joinToString(",") ?: ""
}

fun splitTags(tags: String?): List<String> {
    return if (tags == null || tags == "") {
        emptyList()
    } else {
        tags.split(",")
    }
}

fun toEmojis(tags: List<String>): List<String> {
    return tags.mapNotNull { tag -> toEmoji(tag) }
}

fun toEmoji(tag: String): String? {
    return EmojiManager.getForAlias(tag)?.unicode
}

fun unmatchedTags(tags: List<String>): List<String> {
    return tags.filter { tag -> toEmoji(tag) == null }
}

/**
 * Prepend tags/emojis to message, but only if there is a non-empty title.
 * Otherwise the tags will be prepended to the title.
 */
fun formatMessage(notification: Notification): String {
    return if (notification.title != "") {
        notification.message
    } else {
        val emojis = toEmojis(splitTags(notification.tags))
        if (emojis.isEmpty()) {
            notification.message
        } else {
            emojis.joinToString("") + " " + notification.message
        }
    }
}

/**
 * See above; prepend emojis to title if the title is non-empty.
 * Otherwise, they are prepended to the message.
 */
fun formatTitle(subscription: Subscription, notification: Notification): String {
    return if (notification.title != "") {
        formatTitle(notification)
    } else {
        topicShortUrl(subscription.baseUrl, subscription.topic)
    }
}

fun formatTitle(notification: Notification): String {
    val emojis = toEmojis(splitTags(notification.tags))
    return if (emojis.isEmpty()) {
        notification.title
    } else {
        emojis.joinToString("") + " " + notification.title
    }
}

// Status bar color fading to match action bar, see https://stackoverflow.com/q/51150077/1440785
fun fadeStatusBarColor(window: Window, fromColor: Int, toColor: Int) {
    val statusBarColorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
    statusBarColorAnimation.addUpdateListener { animator ->
        val color = animator.animatedValue as Int
        window.statusBarColor = color
    }
    statusBarColorAnimation.start()
}
