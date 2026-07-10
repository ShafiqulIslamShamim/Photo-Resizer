import java.util.Base64

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.roborazzi) apply false
    id("com.diffplug.spotless") version "7.0.2"
    id("com.google.gms.google-services") version "4.5.0" apply false
    id("com.google.firebase.crashlytics") version "3.0.7" apply false
}

val headerBase64 = "LyoKKiBDb3B5cmlnaHQgKGMpIDIwMjYgU2hhZmlxdWwgSXNsYW0gU2hhbWltCiogR2l0SHViOiBodHRwczovL2dpdGh1Yi5jb20vU2hhZmlxdWxJc2xhbVNoYW1pbS9QaG90by1SZXNpemVyCiogCiogQWxsIFJpZ2h0cyBSZXNlcnZlZC4KKiAKKiBUaGlzIHNvdXJjZSBjb2RlIGlzIG1hZGUgcHVibGljbHkgYXZhaWxhYmxlIHNvbGVseSBmb3Igdmlld2luZywgY29sbGFib3JhdGlvbiwKKiBlZHVjYXRpb25hbCByZWZlcmVuY2UsIGFuZCBzdWJtaXR0aW5nIHB1bGwgcmVxdWVzdHMgdG8gdGhlIG9mZmljaWFsIHJlcG9zaXRvcnkuCiogCiogTm8gcGVybWlzc2lvbiBpcyBncmFudGVkIHRvIGNvcHksIG1vZGlmeSwgcmVkaXN0cmlidXRlLCBzdWJsaWNlbnNlLCBvciB1c2UKKiB0aGlzIHNvdXJjZSBjb2RlLCBpbiB3aG9sZSBvciBpbiBwYXJ0LCBmb3IgcGVyc29uYWwsIGNvbW1lcmNpYWwsIG9yIGFueSBvdGhlcgoqIHB1cnBvc2Ugd2l0aG91dCB0aGUgcHJpb3Igd3JpdHRlbiBwZXJtaXNzaW9uIG9mIHRoZSBjb3B5cmlnaHQgaG9sZGVyLgoqLwo="

val licenseHeader =
    String(
        Base64.getDecoder().decode(headerBase64),
        Charsets.UTF_8,
    )

spotless {
    kotlin {
        target("**/*.kt")
        licenseHeader(licenseHeader)
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts", "**/*.gradle.kts")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("xml") {
        target("**/*.xml")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
