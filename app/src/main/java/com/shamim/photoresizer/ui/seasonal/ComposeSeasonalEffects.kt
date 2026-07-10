/*
* Copyright (c) 2026 Shafiqul Islam Shamim
* GitHub: https://github.com/ShafiqulIslamShamim/Photo-Resizer
*
* All Rights Reserved.
*
* This source code is made publicly available solely for viewing, collaboration,
* educational reference, and submitting pull requests to the official repository.
*
* No permission is granted to copy, modify, redistribute, sublicense, or use
* this source code, in whole or in part, for personal, commercial, or any other
* purpose without the prior written permission of the copyright holder.
*/
package com.shamim.photoresizer.ui.seasonal

import android.content.Context
import android.telephony.TelephonyManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ComposeParticle(
    var x: Float,
    var y: Float,
    var size: Float,
    var speedX: Float,
    var speedY: Float,
    var alpha: Float,
    var angle: Float = 0f,
    var rotationSpeed: Float = 0f,
    var color: Color,
    var waveFrequency: Float = 0f,
    var waveAmplitude: Float = 0f,
    var shapeType: Int =
        0, // 0 = Circle (Snow/Pollen), 1 = Petal (Spring), 2 = Leaf (Fall), 3 = Sunbeam (Summer)
)

data class ComposeRipple(
    val x: Float,
    val y: Float,
    var radius: Float,
    val maxRadius: Float,
    var alpha: Float,
    val speed: Float,
    val color: Color,
)

data class ComposeSplash(
    var x: Float,
    var y: Float,
    val radius: Float,
    var velocityX: Float,
    var velocityY: Float,
    var alpha: Float,
    var scale: Float,
    var lifetime: Float,
    val maxLifetime: Float,
    val color: Color,
)

object ComposeSeasonalEffects {
    /**
     * Inspects the selected season setting. If set to "auto", dynamically resolves the
     * active season ("winter", "spring", "summer", "monsoon", "fall") based on telephony country metadata
     * and month of year.
     *
     * @param selectedSeason User selected season preference string.
     * @param context Application/Activity context.
     * @return Resolved active season identifier string.
     */
    fun getActiveSeason(
        selectedSeason: String,
        context: Context,
    ): String {
        if (selectedSeason != "auto") return selectedSeason

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val country =
            when {
                telephonyManager.networkCountryIso.isNotEmpty() ->
                    telephonyManager.networkCountryIso.uppercase()

                telephonyManager.simCountryIso.isNotEmpty() -> telephonyManager.simCountryIso.uppercase()

                else -> Locale.getDefault().country.uppercase()
            }

        val month = Calendar.getInstance().get(Calendar.MONTH)

        return when (country) {
            "BD",
            "IN",
            -> {
                // South Asia
                when (month) {
                    Calendar.APRIL,
                    Calendar.MAY,
                    -> "summer"
                    Calendar.JUNE,
                    Calendar.JULY,
                    -> "monsoon"
                    Calendar.AUGUST,
                    Calendar.SEPTEMBER,
                    Calendar.OCTOBER,
                    Calendar.NOVEMBER,
                    -> "fall"
                    Calendar.DECEMBER,
                    Calendar.JANUARY,
                    -> "winter"
                    Calendar.FEBRUARY,
                    Calendar.MARCH,
                    -> "spring"
                    else -> "monsoon"
                }
            }

            "AU",
            "NZ",
            "ZA",
            "BR",
            "AR",
            "CL",
            -> {
                // Southern Hemisphere
                when (month) {
                    Calendar.DECEMBER,
                    Calendar.JANUARY,
                    Calendar.FEBRUARY,
                    -> "summer"
                    Calendar.MARCH,
                    Calendar.APRIL,
                    Calendar.MAY,
                    -> "fall"
                    Calendar.JUNE,
                    Calendar.JULY,
                    Calendar.AUGUST,
                    -> "winter"
                    Calendar.SEPTEMBER,
                    Calendar.OCTOBER,
                    Calendar.NOVEMBER,
                    -> "spring"
                    else -> "winter"
                }
            }

            else -> {
                // Northern Hemisphere
                when (month) {
                    Calendar.MARCH,
                    Calendar.APRIL,
                    Calendar.MAY,
                    -> "spring"
                    Calendar.JUNE,
                    Calendar.JULY,
                    Calendar.AUGUST,
                    -> "summer"
                    Calendar.SEPTEMBER,
                    Calendar.OCTOBER,
                    Calendar.NOVEMBER,
                    -> "fall"
                    Calendar.DECEMBER,
                    Calendar.JANUARY,
                    Calendar.FEBRUARY,
                    -> "winter"
                    else -> "winter"
                }
            }
        }
    }

    /**
     * Populates the active list of floating background particles, adjusting particle density counts
     * depending on the active season.
     *
     * @param particles The target mutable list to initialize.
     * @param season The active season string.
     * @param width Screen canvas width.
     * @param height Screen canvas height.
     * @param primaryColor Active theme primary color.
     * @param secondaryColor Active theme secondary color.
     * @param tertiaryColor Active theme tertiary color.
     */
    fun initializeSeasonalParticles(
        particles: MutableList<ComposeParticle>,
        season: String,
        width: Float,
        height: Float,
        primaryColor: Color,
        secondaryColor: Color,
        tertiaryColor: Color,
    ) {
        val count =
            when (season) {
                "winter" -> 55
                "spring" -> 45
                "summer" -> 30
                "monsoon" -> 95
                "fall" -> 25
                else -> 50
            }

        particles.clear()
        repeat(count) {
            particles.add(
                createRandomParticle(
                    season,
                    width,
                    height,
                    true,
                    primaryColor,
                    secondaryColor,
                    tertiaryColor,
                ),
            )
        }
    }

    /**
     * Instantiates a single snowflake, leaf, raindrop, flower petal, or sun-glare particle with
     * randomized scaling, velocities, rotation angles, colors, and wave frequencies matching seasonal themes.
     *
     * @param season Active resolved season identifier.
     * @param width Layout container boundary width.
     * @param height Layout container boundary height.
     * @param randomY True to randomize initial Y coordinate, false to spawn at the top boundary.
     * @param primaryColor Active theme primary color.
     * @param secondaryColor Active theme secondary color.
     * @param tertiaryColor Active theme tertiary color.
     * @return Fully initialized randomized ComposeParticle.
     */
    fun createRandomParticle(
        season: String,
        width: Float,
        height: Float,
        randomY: Boolean,
        primaryColor: Color,
        secondaryColor: Color,
        tertiaryColor: Color,
    ): ComposeParticle {
        val rx = Random.nextFloat() * width
        val ry = if (randomY) Random.nextFloat() * height else -50f

        return when (season) {
            "winter" -> {
                val size = Random.nextFloat() * 6f + 5f
                val speedY = Random.nextFloat() * 1.3f + 0.6f
                val speedX = Random.nextFloat() * 0.4f - 0.2f
                val alpha = Random.nextFloat() * 0.5f + 0.35f
                val angle = Random.nextFloat() * 360f
                val rotationSpeed = Random.nextFloat() * 0.8f - 0.4f
                val color = if (Random.nextBoolean()) primaryColor else secondaryColor
                ComposeParticle(
                    x = rx,
                    y = ry,
                    size = size,
                    speedX = speedX,
                    speedY = speedY,
                    alpha = alpha,
                    angle = angle,
                    rotationSpeed = rotationSpeed,
                    color = color.copy(alpha = alpha),
                    shapeType = 4,
                )
            }
            "spring" -> {
                val isPetal = Random.nextBoolean()
                val size = if (isPetal) Random.nextFloat() * 18f + 12f else Random.nextFloat() * 5f + 3f
                val speedY = Random.nextFloat() * 1.0f + 0.6f
                val speedX = Random.nextFloat() * 0.4f - 0.2f
                val alpha = Random.nextFloat() * 0.6f + 0.3f
                val angle = Random.nextFloat() * 360f
                val rotationSpeed = Random.nextFloat() * 1.2f - 0.6f
                val color = if (isPetal) primaryColor else tertiaryColor
                ComposeParticle(
                    x = rx,
                    y = ry,
                    size = size,
                    speedX = speedX,
                    speedY = speedY,
                    alpha = alpha,
                    angle = angle,
                    rotationSpeed = rotationSpeed,
                    color = color.copy(alpha = alpha),
                    waveFrequency = Random.nextFloat() * 0.015f + 0.008f,
                    waveAmplitude = Random.nextFloat() * 1.2f + 0.4f,
                    shapeType = if (isPetal) 1 else 0,
                )
            }
            "summer" -> {
                val size = Random.nextFloat() * 3.5f + 2.5f
                val speedY = -(Random.nextFloat() * 0.5f + 0.25f)
                val speedX = Random.nextFloat() * 0.4f - 0.2f
                val alpha = Random.nextFloat() * 0.5f + 0.25f
                val angle = Random.nextFloat() * 360f
                val rotationSpeed = Random.nextFloat() * 0.5f - 0.25f
                // Sunny color: mix of primary color and warm glowing yellow
                val color = if (Random.nextBoolean()) primaryColor else Color(0xFFFFEB3B)
                ComposeParticle(
                    x = rx,
                    y = if (randomY) ry else height + 20f,
                    size = size,
                    speedX = speedX,
                    speedY = speedY,
                    alpha = alpha,
                    angle = angle,
                    rotationSpeed = rotationSpeed,
                    color = color.copy(alpha = alpha),
                    shapeType = 3,
                )
            }
            "monsoon" -> {
                val depth = Random.nextInt(3) // 0 = far, 1 = mid, 2 = close
                val length: Float
                val speedY: Float
                val alpha: Float
                val thickness: Float
                when (depth) {
                    0 -> { // Far (small, slow, dim)
                        length = Random.nextFloat() * 15f + 15f
                        speedY = Random.nextFloat() * 5f + 12f
                        alpha = Random.nextFloat() * 0.12f + 0.1f
                        thickness = 1.0f
                    }
                    1 -> { // Mid (medium)
                        length = Random.nextFloat() * 22f + 25f
                        speedY = Random.nextFloat() * 8f + 18f
                        alpha = Random.nextFloat() * 0.18f + 0.18f
                        thickness = 1.8f
                    }
                    else -> { // Near (long, fast, prominent)
                        length = Random.nextFloat() * 30f + 40f
                        speedY = Random.nextFloat() * 10f + 26f
                        alpha = Random.nextFloat() * 0.22f + 0.32f
                        thickness = 2.6f
                    }
                }
                val speedX =
                    -speedY * 0.08f // slanting matches fall speed perfectly for seamless diagonal falling

                val rainColor =
                    if (Random.nextBoolean()) {
                        primaryColor
                    } else {
                        Color(0xFF80DEEA) // Beautiful cyan-blue water droplet color
                    }
                ComposeParticle(
                    x = rx,
                    y = ry,
                    size = length,
                    speedX = speedX,
                    speedY = speedY,
                    alpha = alpha,
                    color = rainColor.copy(alpha = alpha),
                    shapeType = 0,
                    waveAmplitude = thickness, // Store thickness multiplier in waveAmplitude
                )
            }
            "fall" -> {
                val size = Random.nextFloat() * 22f + 14f
                val speedY = Random.nextFloat() * 0.9f + 0.5f
                val speedX = Random.nextFloat() * 0.3f - 0.15f
                val alpha = Random.nextFloat() * 0.65f + 0.3f
                val angle = Random.nextFloat() * 360f
                val rotationSpeed = Random.nextFloat() * 0.8f - 0.4f
                val color = if (Random.nextBoolean()) primaryColor else tertiaryColor
                ComposeParticle(
                    x = rx,
                    y = ry,
                    size = size,
                    speedX = speedX,
                    speedY = speedY,
                    alpha = alpha,
                    angle = angle,
                    rotationSpeed = rotationSpeed,
                    color = color.copy(alpha = alpha),
                    waveFrequency = Random.nextFloat() * 0.012f + 0.004f,
                    waveAmplitude = Random.nextFloat() * 1.8f + 0.8f,
                    shapeType = 2,
                )
            }
            else -> {
                ComposeParticle(
                    x = rx,
                    y = ry,
                    size = 5f,
                    speedX = 0f,
                    speedY = 1f,
                    alpha = 0.5f,
                    color = primaryColor,
                    shapeType = 0,
                )
            }
        }
    }

    /**
     * Frame-by-frame simulation updater. Updates coordinates, scales, sway angles, rotational angles,
     * lifetimes, and alpha coefficients of background particles, ripples, and touches.
     *
     * @param particles Current active background drifting particles list.
     * @param ripples Active water ripples list.
     * @param splashes Active tap splashes list.
     * @param season Active season theme identifier.
     * @param deltaMillis Time delta since last rendering frame tick in milliseconds.
     * @param primaryColor Active theme primary color.
     * @param secondaryColor Active theme secondary color.
     * @param tertiaryColor Active theme tertiary color.
     */
    fun updateSeasonalParticles(
        particles: MutableList<ComposeParticle>,
        ripples: MutableList<ComposeRipple>,
        splashes: MutableList<ComposeSplash>,
        season: String,
        deltaMillis: Float,
        primaryColor: Color,
        secondaryColor: Color,
        tertiaryColor: Color,
    ) {
        val speedFactor = deltaMillis * 0.06f

        // 1. Update ripples
        val rippleIterator = ripples.iterator()
        while (rippleIterator.hasNext()) {
            val ripple = rippleIterator.next()
            if (ripple.radius >= 0f) {
                ripple.radius += ripple.speed * speedFactor
                ripple.alpha = (1f - (ripple.radius / ripple.maxRadius)).coerceIn(0f, 1f)
            } else {
                // Delayed ring
                ripple.radius += ripple.speed * speedFactor
            }
            if (ripple.radius >= ripple.maxRadius) {
                rippleIterator.remove()
            }
        }

        // 2. Update splashes
        val splashIterator = splashes.iterator()
        while (splashIterator.hasNext()) {
            val splash = splashIterator.next()
            splash.lifetime += deltaMillis / 1000f
            val progress = (splash.lifetime / splash.maxLifetime).coerceIn(0f, 1f)

            splash.x += splash.velocityX * (deltaMillis / 1000f)
            splash.y += splash.velocityY * (deltaMillis / 1000f)

            if (season == "monsoon") {
                splash.velocityY += 250f * (deltaMillis / 1000f) // gravity
            }

            splash.scale = 1f - progress
            splash.alpha = 1f - progress

            if (splash.lifetime >= splash.maxLifetime) {
                splashIterator.remove()
            }
        }

        // 3. Update background drifting particles
        particles.forEach { particle ->
            // Normal fallback speed logic
            particle.y += particle.speedY * speedFactor
            particle.x += particle.speedX * speedFactor

            // Swaying & Rotation
            if (particle.shapeType == 1 || particle.shapeType == 2) {
                particle.x +=
                    sin(particle.y * particle.waveFrequency) * particle.waveAmplitude * speedFactor
                particle.angle += particle.rotationSpeed * speedFactor
            } else if (particle.shapeType == 4) { // Winter Snowflake
                // Gentle realistic side-to-side drift/sway
                particle.x += sin(particle.y * 0.015f) * 0.45f * speedFactor
                particle.angle += particle.rotationSpeed * speedFactor
            } else if (particle.shapeType == 3) { // Summer Sparkle
                particle.angle += particle.rotationSpeed * speedFactor
            }
        }
    }

    /**
     * Renders all active background particles, water splash drops, and expand rings directly to the
     * Canvas draw scope.
     *
     * @param particles List of current background particles.
     * @param ripples List of active water expansion rings.
     * @param splashes List of active point splashes.
     * @param season Active season style name.
     * @param primaryColor Current primary theme color.
     * @param secondaryColor Current secondary theme color.
     * @param tertiaryColor Current tertiary theme color.
     * @param strokeWidthPx Standard line stroke width scale in pixels.
     */
    fun DrawScope.drawSeasonalParticles(
        particles: MutableList<ComposeParticle>,
        ripples: MutableList<ComposeRipple>,
        splashes: MutableList<ComposeSplash>,
        season: String,
        primaryColor: Color,
        secondaryColor: Color,
        tertiaryColor: Color,
        strokeWidthPx: Float,
    ) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f) return

        // 1. Recycle particles
        particles.forEachIndexed { _, particle ->
            var needsReset = false
            if (season == "summer") {
                if (particle.y < -30f) needsReset = true
            } else {
                if (particle.y > height + 30f) needsReset = true
            }

            // horizontal boundaries wrap
            if (particle.x < -40f) {
                particle.x = width + 20f
            } else if (particle.x > width + 40f) {
                particle.x = -20f
            }

            if (needsReset) {
                val newP =
                    createRandomParticle(
                        season,
                        width,
                        height,
                        false,
                        primaryColor,
                        secondaryColor,
                        tertiaryColor,
                    )
                particle.x = newP.x
                particle.y = newP.y
                particle.size = newP.size
                particle.speedX = newP.speedX
                particle.speedY = newP.speedY
                particle.alpha = newP.alpha
                particle.angle = newP.angle
                particle.rotationSpeed = newP.rotationSpeed
                particle.color = newP.color
                particle.waveFrequency = newP.waveFrequency
                particle.waveAmplitude = newP.waveAmplitude
                particle.shapeType = newP.shapeType
            }
        }

        // Special Rainy Season automatic splash ripples at the bottom
        if (season == "monsoon") {
            particles.forEach { particle ->
                if (particle.y >= height - 15f && particle.y != -9999f) {
                    // Create puddle ripples
                    ripples.add(
                        ComposeRipple(
                            x = particle.x,
                            y = height - 12f,
                            radius = 0f,
                            maxRadius = Random.nextFloat() * 12f + 6f,
                            alpha = 0.5f,
                            speed = 1.6f,
                            color = particle.color,
                        ),
                    )
                    // Create realistic splashing droplets shooting upward!
                    repeat(Random.nextInt(1, 3)) {
                        val angle = Random.nextFloat() * 100f + 220f // upward range: 220 to 320 degrees
                        val speed = Random.nextFloat() * 45f + 25f
                        val rad = Math.toRadians(angle.toDouble())
                        splashes.add(
                            ComposeSplash(
                                x = particle.x,
                                y = height - 12f,
                                radius = Random.nextFloat() * 1.5f + 0.8f,
                                velocityX = cos(rad).toFloat() * speed,
                                velocityY = sin(rad).toFloat() * speed - 20f,
                                alpha = 0.8f,
                                scale = 1.0f,
                                lifetime = 0f,
                                maxLifetime = Random.nextFloat() * 0.15f + 0.12f,
                                color = particle.color,
                            ),
                        )
                    }

                    // Reset to top immediately
                    val topDrop =
                        createRandomParticle(
                            season,
                            width,
                            height,
                            false,
                            primaryColor,
                            secondaryColor,
                            tertiaryColor,
                        )
                    particle.x = topDrop.x
                    particle.y = topDrop.y
                    particle.size = topDrop.size
                    particle.speedX = topDrop.speedX
                    particle.speedY = topDrop.speedY
                    particle.alpha = topDrop.alpha
                    particle.color = topDrop.color
                    particle.waveAmplitude = topDrop.waveAmplitude
                }
            }
        }

        // 2. Draw ripples
        ripples.forEach { ripple ->
            if (ripple.radius >= 0f && ripple.alpha > 0f) {
                val progress = ripple.radius / ripple.maxRadius
                drawOval(
                    color = ripple.color.copy(alpha = ripple.alpha * 0.7f),
                    topLeft = Offset(ripple.x - ripple.radius, ripple.y - (ripple.radius * 0.3f)),
                    size = Size(ripple.radius * 2f, ripple.radius * 0.6f),
                    style = Stroke(width = strokeWidthPx * (1.2f - progress)),
                )
            }
        }

        // 3. Draw splashes
        splashes.forEach { splash ->
            if (splash.alpha > 0f && splash.scale > 0f) {
                drawCircle(
                    color = splash.color.copy(alpha = splash.alpha),
                    radius = splash.radius * splash.scale,
                    center = Offset(splash.x, splash.y),
                )
            }
        }

        // 4. Draw background particles
        particles.forEach { particle ->
            when (particle.shapeType) {
                0 -> { // Circle (Snow / Pollen / Raindrops in monsoon are lines)
                    if (season == "monsoon") {
                        // Slanted beautiful raindrop lines matching speedX slope
                        val thickness = particle.waveAmplitude.takeIf { it > 0f } ?: 1.0f
                        val slantX = particle.speedX * 1.5f
                        drawLine(
                            color = particle.color,
                            start = Offset(particle.x, particle.y),
                            end = Offset(particle.x + slantX, particle.y + particle.size),
                            strokeWidth = strokeWidthPx * thickness,
                        )
                    } else {
                        // Standard Winter Snow / Spring Pollen circles
                        // Draw soft dynamic glow
                        drawCircle(
                            color = particle.color.copy(alpha = particle.alpha * 0.25f),
                            radius = particle.size * 2.2f,
                            center = Offset(particle.x, particle.y),
                        )
                        drawCircle(
                            color = particle.color,
                            radius = particle.size,
                            center = Offset(particle.x, particle.y),
                        )
                    }
                }
                1 -> { // Spring Petals (cherry blossom pink / theme primary)
                    rotate(particle.angle, Offset(particle.x, particle.y)) {
                        drawOval(
                            color = particle.color,
                            topLeft = Offset(particle.x - particle.size / 2f, particle.y - particle.size / 3f),
                            size = Size(particle.size, particle.size / 1.5f),
                        )
                    }
                }
                2 -> { // Fall Leaf (Beautiful almond-shaped leaves rotating)
                    rotate(particle.angle, Offset(particle.x, particle.y)) {
                        val leafPath =
                            Path().apply {
                                moveTo(particle.x, particle.y - particle.size / 2f)
                                quadraticTo(
                                    particle.x + particle.size / 2f,
                                    particle.y,
                                    particle.x,
                                    particle.y + particle.size / 2f,
                                )
                                quadraticTo(
                                    particle.x - particle.size / 2f,
                                    particle.y,
                                    particle.x,
                                    particle.y - particle.size / 2f,
                                )
                            }
                        drawPath(path = leafPath, color = particle.color)
                    }
                }
                3 -> { // Summer Warm rays/sparkles rising
                    rotate(particle.angle, Offset(particle.x, particle.y)) {
                        // Soft solar center glow
                        drawCircle(
                            color = particle.color.copy(alpha = particle.alpha * 0.35f),
                            radius = particle.size * 1.5f,
                            center = Offset(particle.x, particle.y),
                        )
                        // Elegant 4-pointed lens flare/sparkle
                        val sparklePath =
                            Path().apply {
                                moveTo(particle.x, particle.y - particle.size * 2.2f)
                                quadraticTo(particle.x, particle.y, particle.x + particle.size * 2.2f, particle.y)
                                quadraticTo(particle.x, particle.y, particle.x, particle.y + particle.size * 2.2f)
                                quadraticTo(particle.x, particle.y, particle.x - particle.size * 2.2f, particle.y)
                                quadraticTo(particle.x, particle.y, particle.x, particle.y - particle.size * 2.2f)
                            }
                        drawPath(path = sparklePath, color = particle.color)
                    }
                }
                4 -> { // Winter Snowflake
                    rotate(particle.angle, Offset(particle.x, particle.y)) {
                        // 6-pointed procedural snowflake!
                        for (i in 0 until 6) {
                            val angleRad = Math.toRadians((i * 60).toDouble())
                            val endX = particle.x + cos(angleRad).toFloat() * particle.size
                            val endY = particle.y + sin(angleRad).toFloat() * particle.size

                            // Main spoke
                            drawLine(
                                color = particle.color,
                                start = Offset(particle.x, particle.y),
                                end = Offset(endX, endY),
                                strokeWidth = strokeWidthPx * 1.3f,
                            )

                            // Delicate sub-branches / V-shapes
                            val subBranchLen = particle.size * 0.35f
                            val midX = particle.x + cos(angleRad).toFloat() * (particle.size * 0.55f)
                            val midY = particle.y + sin(angleRad).toFloat() * (particle.size * 0.55f)

                            // Upper sub-branch angle (angleRad - 30 degrees)
                            val branchAngle1 = angleRad - Math.toRadians(30.0)
                            drawLine(
                                color = particle.color,
                                start = Offset(midX, midY),
                                end =
                                    Offset(
                                        midX + cos(branchAngle1).toFloat() * subBranchLen,
                                        midY + sin(branchAngle1).toFloat() * subBranchLen,
                                    ),
                                strokeWidth = strokeWidthPx * 0.9f,
                            )

                            // Lower sub-branch angle (angleRad + 30 degrees)
                            val branchAngle2 = angleRad + Math.toRadians(30.0)
                            drawLine(
                                color = particle.color,
                                start = Offset(midX, midY),
                                end =
                                    Offset(
                                        midX + cos(branchAngle2).toFloat() * subBranchLen,
                                        midY + sin(branchAngle2).toFloat() * subBranchLen,
                                    ),
                                strokeWidth = strokeWidthPx * 0.9f,
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Programmatically spawns responsive ripples or splash-burst feedback at tap coordinates
     * matching the current active season theme (e.g. blossom leaves, winter snow-spray, or water rings).
     *
     * @param ripples Mutable target ripples list.
     * @param splashes Mutable target point-splashes list.
     * @param season Resolved season identifier.
     * @param x Tap event horizontal coordinate in pixels.
     * @param y Tap event vertical coordinate in pixels.
     * @param primaryColor Current primary theme color.
     * @param secondaryColor Current secondary theme color.
     * @param tertiaryColor Current tertiary theme color.
     */
    fun addTouchFeedback(
        ripples: MutableList<ComposeRipple>,
        splashes: MutableList<ComposeSplash>,
        season: String,
        x: Float,
        y: Float,
        primaryColor: Color,
        secondaryColor: Color,
        tertiaryColor: Color,
    ) {
        when (season) {
            "winter" -> {
                repeat(8) {
                    val angle = Random.nextFloat() * 360f
                    val speed = Random.nextFloat() * 120f + 40f
                    val rad = Math.toRadians(angle.toDouble())
                    splashes.add(
                        ComposeSplash(
                            x = x,
                            y = y,
                            radius = Random.nextFloat() * 3.5f + 1.5f,
                            velocityX = cos(rad).toFloat() * speed,
                            velocityY = sin(rad).toFloat() * speed,
                            alpha = 0.9f,
                            scale = 1.0f,
                            lifetime = 0f,
                            maxLifetime = Random.nextFloat() * 0.35f + 0.25f,
                            color = if (Random.nextBoolean()) primaryColor else secondaryColor,
                        ),
                    )
                }
            }
            "spring" -> {
                repeat(6) {
                    val angle = Random.nextFloat() * 360f
                    val speed = Random.nextFloat() * 100f + 30f
                    val rad = Math.toRadians(angle.toDouble())
                    splashes.add(
                        ComposeSplash(
                            x = x,
                            y = y,
                            radius = Random.nextFloat() * 5.5f + 3.5f,
                            velocityX = cos(rad).toFloat() * speed,
                            velocityY = sin(rad).toFloat() * speed,
                            alpha = 0.95f,
                            scale = 1.0f,
                            lifetime = 0f,
                            maxLifetime = Random.nextFloat() * 0.45f + 0.3f,
                            color = primaryColor,
                        ),
                    )
                }
            }
            "summer" -> {
                ripples.add(
                    ComposeRipple(
                        x = x,
                        y = y,
                        radius = 0f,
                        maxRadius = 70f,
                        alpha = 0.9f,
                        speed = 3.5f,
                        color = primaryColor,
                    ),
                )
                repeat(5) {
                    val angle = Random.nextFloat() * 360f
                    val speed = Random.nextFloat() * 90f + 30f
                    val rad = Math.toRadians(angle.toDouble())
                    splashes.add(
                        ComposeSplash(
                            x = x,
                            y = y,
                            radius = Random.nextFloat() * 4f + 1.5f,
                            velocityX = cos(rad).toFloat() * speed,
                            velocityY = sin(rad).toFloat() * speed,
                            alpha = 0.85f,
                            scale = 1.0f,
                            lifetime = 0f,
                            maxLifetime = Random.nextFloat() * 0.35f + 0.25f,
                            color = secondaryColor,
                        ),
                    )
                }
            }
            "monsoon" -> {
                ripples.add(
                    ComposeRipple(
                        x = x,
                        y = y,
                        radius = 0f,
                        maxRadius = 55f,
                        alpha = 0.9f,
                        speed = 2.8f,
                        color = primaryColor,
                    ),
                )
                if (Random.nextBoolean()) {
                    ripples.add(
                        ComposeRipple(
                            x = x,
                            y = y,
                            radius = -8f,
                            maxRadius = 40f,
                            alpha = 0.6f,
                            speed = 2.2f,
                            color = secondaryColor,
                        ),
                    )
                }
                repeat(5) {
                    val angle = Random.nextFloat() * 180f + 180f
                    val speed = Random.nextFloat() * 100f + 50f
                    val rad = Math.toRadians(angle.toDouble())
                    splashes.add(
                        ComposeSplash(
                            x = x,
                            y = y,
                            radius = Random.nextFloat() * 3f + 1.2f,
                            velocityX = cos(rad).toFloat() * speed,
                            velocityY = sin(rad).toFloat() * speed - 35f,
                            alpha = 0.9f,
                            scale = 1.0f,
                            lifetime = 0f,
                            maxLifetime = Random.nextFloat() * 0.32f + 0.25f,
                            color = primaryColor,
                        ),
                    )
                }
            }
            "fall" -> {
                repeat(4) {
                    val angle = Random.nextFloat() * 360f
                    val speed = Random.nextFloat() * 70f + 30f
                    val rad = Math.toRadians(angle.toDouble())
                    splashes.add(
                        ComposeSplash(
                            x = x,
                            y = y,
                            radius = Random.nextFloat() * 7f + 5f,
                            velocityX = cos(rad).toFloat() * speed,
                            velocityY = sin(rad).toFloat() * speed,
                            alpha = 0.9f,
                            scale = 1.0f,
                            lifetime = 0f,
                            maxLifetime = Random.nextFloat() * 0.55f + 0.35f,
                            color = if (Random.nextBoolean()) primaryColor else tertiaryColor,
                        ),
                    )
                }
            }
        }
    }
}

/**
 * Root Composable overlay wrapping the entire application layout space.
 * Hooks frame ticker clocks, manages simulation lifecycles, handles taps,
 * and draws gorgeous ambient particle, leaf, petal, rain, or snow drift effects.
 *
 * @param selectedSeason Selected season config option ("auto", "winter", "spring", "summer", "monsoon", "fall").
 * @param ripples SnapshotStateList mapping global touch-ripple feedback.
 * @param splashes SnapshotStateList mapping global splash-burst particle feedback.
 */
@Composable
fun ComposeSeasonalEffectsOverlay(
    selectedSeason: String,
    ripples: SnapshotStateList<ComposeRipple>,
    splashes: SnapshotStateList<ComposeSplash>,
) {
    val context = LocalContext.current
    val season =
        remember(selectedSeason) { ComposeSeasonalEffects.getActiveSeason(selectedSeason, context) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val particles =
        remember(season) {
            ripples.clear()
            splashes.clear()
            mutableStateListOf<ComposeParticle>()
        }

    var lastTimeNanos by remember(season) { mutableStateOf(0L) }
    var tick by remember { mutableStateOf(0L) }
    val strokeWidthPx = 1.5.dp.value

    LaunchedEffect(season) {
        lastTimeNanos = 0L
    }

    LaunchedEffect(season) {
        while (true) {
            withFrameNanos { frameTime ->
                if (lastTimeNanos == 0L) {
                    lastTimeNanos = frameTime
                }
                val deltaMillis = ((frameTime - lastTimeNanos) / 1_000_000f).coerceIn(0f, 50f)
                lastTimeNanos = frameTime

                ComposeSeasonalEffects.updateSeasonalParticles(
                    particles,
                    ripples,
                    splashes,
                    season,
                    deltaMillis,
                    primaryColor,
                    secondaryColor,
                    tertiaryColor,
                )
                tick = frameTime
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val currentTick = tick // Read the tick state to force redraw on every frame
        val width = size.width
        val height = size.height

        if (width > 0f && height > 0f) {
            if (particles.isEmpty()) {
                ComposeSeasonalEffects.initializeSeasonalParticles(
                    particles,
                    season,
                    width,
                    height,
                    primaryColor,
                    secondaryColor,
                    tertiaryColor,
                )
            }

            with(ComposeSeasonalEffects) {
                drawSeasonalParticles(
                    particles,
                    ripples,
                    splashes,
                    season,
                    primaryColor,
                    secondaryColor,
                    tertiaryColor,
                    strokeWidthPx,
                )
            }
        }
    }
}
