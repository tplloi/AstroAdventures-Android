package com.roy.ui

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.view.Gravity
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.lifecycleScope
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet.ORDERING_SEQUENTIAL
import androidx.viewbinding.ViewBinding
import com.roy.R
import com.roy.data.BulletStore.Companion.getAmountScore
import com.roy.data.Score
import com.roy.data.Score.saveScore
import com.roy.data.Score.scoreFlow
import com.roy.databinding.SceneGameBinding
import com.roy.ext.moreApp
import com.roy.ext.openBrowserPolicy
import com.roy.ext.rateApp
import com.roy.ext.shareApp
import com.roy.ui.game.views.bullets.BulletView
import com.roy.utils.PLAYER_BULLET_SOUND
import com.roy.utils.SoundData
import com.roy.utils.SoundManager
import com.roy.utils.addTransition
import com.roy.utils.onEnd
import com.roy.utils.transitionSet
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@SuppressLint("SetTextI18n")
fun MainActivity.observeScreenStates() {
    lifecycleScope.launchWhenCreated {
        viewModel.observeScreenState()
            .collect {
                uiEventJob.cancel()
                when (it) {
                    ScreenStates.AppInit -> {
                        backgroundMusicManager.startPlaying()
                        transitionTo(
                            toScene = initScene.scene,
                            transition = Fade(Fade.MODE_IN)
                        )
                    }

                    ScreenStates.GameMenu -> {
                        backgroundMusicManager.startPlaying()
                        gameMenuScene.binding.apply {
                            val transition = transitionSet {

                                ordering = ORDERING_SEQUENTIAL

                                addTransition(Slide(Gravity.TOP)) {
                                    addTarget(logoView)
                                    duration = 300L
                                }

                                addTransition(Slide(Gravity.BOTTOM)) {
                                    addTarget(btnStart)
                                    duration = 300L
                                }

                                addTransition(Slide(Gravity.BOTTOM)) {
                                    addTarget(btnViewScores)
                                    duration = 300L
                                }

                                addTransition(Slide(Gravity.BOTTOM)) {
                                    addTarget(llRateMore)
                                    duration = 300L
                                }

                                addTransition(Slide(Gravity.BOTTOM)) {
                                    addTarget(llSharePolicy)
                                    duration = 300L
                                }

                                addTransition(Slide(Gravity.BOTTOM)) {
                                    addTarget(btnExit)
                                    duration = 300L
                                }

                                interpolator = AccelerateDecelerateInterpolator()
                            }
                            if (viewModel.previousState == ScreenStates.GameOver) {
                                transitionFromTo(
                                    fromScene = gameOverScene.scene,
                                    toScene = gameMenuScene.scene,
                                    transition = transition
                                )
                            } else {
                                transitionFromTo(
                                    fromScene = initScene.scene,
                                    toScene = gameMenuScene.scene,
                                    transition = transition
                                )
                            }
                        }
                        gameMenuScene.binding.btnStart.setOnClickListener {
                            backgroundMusicManager.stopPlaying()
                            if (com.roy.data.LevelInfo.hasPlayedTutorial) {
                                com.roy.data.LevelInfo.resetLevel()
                            }
                            viewModel.updateUIState(ScreenStates.LevelStart)
                        }
                        gameMenuScene.binding.btnViewScores.setOnClickListener {
                            viewModel.updateUIState(ScreenStates.ViewHighScores)
                        }
                        gameMenuScene.binding.btnRate.setOnClickListener {
                            rateApp(packageName)
                        }
                        gameMenuScene.binding.btnMore.setOnClickListener {
                            moreApp("Roy93Group")
                        }
                        gameMenuScene.binding.btnShare.setOnClickListener {
                            shareApp()
                        }
                        gameMenuScene.binding.btnPolicy.setOnClickListener {
                            openBrowserPolicy()
                        }
                        gameMenuScene.binding.btnExit.setOnClickListener {
                            finish()
                        }
                    }

                    ScreenStates.LevelStart -> {
                        levelStartScene.binding.timerView.text = ""
                        levelStartScene.binding.levelStartText.text =
                            getString(R.string.level_start, com.roy.data.LevelInfo.level)
                        if (com.roy.data.LevelInfo.level == 1) {
                            Score.resetScore()
                            com.roy.data.PlayerHealthInfo.resetHealth()
                        }
                        transitionTo(
                            toScene = levelStartScene.scene,
                            transition = Fade(Fade.MODE_IN).apply {
                                onEnd {
                                    uiEventJob = lifecycleScope.launchWhenCreated {
                                        if (isActive) {
                                            (3 downTo 1).forEach { item ->
                                                levelStartScene.binding.timerView.text =
                                                    item.toString()
                                                delay(1000)
                                            }
                                            if (com.roy.data.LevelInfo.hasPlayedTutorial)
                                                viewModel.updateUIState(ScreenStates.StartGame)
                                            else
                                                viewModel.updateUIState(ScreenStates.StartLevelZero)
                                        }
                                    }
                                }
                            })
                    }

                    is ScreenStates.StartGame -> {
                        resetGameScene()
                        gameScene.binding.apply {
                            val bulletStore =
                                com.roy.data.BulletStore(com.roy.data.BulletStore.REFILL)
                            enemyClusterView.bulletStore = bulletStore
                            spaceShipView.bulletStore = bulletStore
                            spaceShipView.onHitCallBack = {
                                containerView.startShakeAnimation(
                                    duration = 50,
                                    offset = 10,
                                    repeatCount = 4
                                )
                            }
                            spaceShipView.onAmmoCollectedCallback = {
                                ammoCountView.onBulletCollected()
                            }
                            lifecycleScope.launchWhenCreated {
                                scoreFlow().collect { score ->
                                    if (isActive) {
                                        tvScore.text = getString(R.string.score_text, score)
                                    }
                                }
                            }

                            lifecycleScope.launchWhenCreated {
                                bulletStore.bulletCountFlow().collect { ammoCount ->
                                    ammoCountView.setBulletCount(
                                        ammoCount = ammoCount,
                                        maxCount = bulletStore.maxCount
                                    )
                                }
                            }

                            transitionFromTo(
                                fromScene = levelStartScene.scene,
                                toScene = gameScene.scene,
                                transition = transitionSet {
                                    addTransition(Slide(Gravity.BOTTOM)) {
                                        duration = 1200
                                        interpolator = AccelerateDecelerateInterpolator()
                                        addTarget(spaceShipView)
                                    }
                                    addTransition(Slide(Gravity.TOP)) {
                                        duration = 2200
                                        interpolator = LinearInterpolator()
                                        addTarget(enemyClusterView)
                                    }
                                    onEnd {
                                        startGame(gameScene.binding)

                                        val itemSoundManger = SoundManager(
                                            context = applicationContext,
                                            soundData = SoundData(
                                                R.raw.music_player_bullet,
                                                PLAYER_BULLET_SOUND
                                            ),
                                            lifecycle = lifecycle
                                        )

                                        dropsView.setSoundManager(itemSoundManger)
                                        bulletView.setSoundManager(itemSoundManger)

                                        bulletView.setOnClickListener {
                                            if (bulletStore.getAmmoCount() != 0) {
                                                bulletStore.updateInventory()
                                                bulletView.fire(
                                                    x = spaceShipView.getShipX(),
                                                    y = spaceShipView.getShipY(),
                                                    sender = BulletView.Sender.PLAYER
                                                )
                                            } else {
                                                viewModel.updateUIState(ScreenStates.RanOutOfAmmo)
                                            }
                                        }
                                    }
                                })
                        }
                    }

                    is ScreenStates.StartLevelZero -> {
                        startLevelZero()
                    }

                    is ScreenStates.LevelComplete -> {
                        transitionFromTo(
                            fromScene = gameScene.scene,
                            toScene = levelCompleteScene.scene,
                            transition = Fade(Fade.MODE_IN).apply {
                                duration = 700
                            })

                        levelCompleteScene.binding.apply {
                            tvSuccessText.text =
                                getString(R.string.level_complete, com.roy.data.LevelInfo.level)
                            tvScoreViewValue.text = scoreFlow().value.toString()

                            uiEventJob = lifecycleScope.launchWhenCreated {
                                delay(1500L)
                                val constraint2 = ConstraintSet()
                                constraint2.clone(
                                    root.context,
                                    R.layout.scene_level_complete_scoreboard
                                )

                                TransitionManager.beginDelayedTransition(root)
                                constraint2.applyTo(root)


                                lifecycleScope.launchWhenCreated {
                                    delay(800L)
                                    bulletCountValueView.addNewValue(getAmountScore(it.bulletCount).toFloat()) {
                                        val newScore =
                                            scoreFlow().value.toFloat() + getAmountScore(it.bulletCount)
                                        Score.updateScore(getAmountScore(it.bulletCount).toLong())
                                        totalScoreValueView.addNewValue(newScore)
                                    }
                                }

                                btnMainMenu.setOnClickListener {
                                    viewModel.updateUIState(ScreenStates.GameMenu)
                                }


                                btnContinue.setOnClickListener {
                                    com.roy.data.LevelInfo.increment()
                                    viewModel.updateUIState(ScreenStates.LevelStartWarp)
                                }
                            }
                            saveScore(lifecycleScope)
                        }
                    }

                    ScreenStates.ViewHighScores -> {
                        transitionTo(
                            toScene = highScoreScene.scene,
                            transition = Fade(Fade.MODE_IN)
                        )
                        highScoreScene.binding.apply {
                            lifecycleScope.launch {
                                com.roy.data.DataStoreHelper.getHighScore()
                                    .zip(com.roy.data.DataStoreHelper.getMaxLevels()) { highScore, maxLevels ->
                                        Pair(first = maxLevels, second = highScore)
                                    }.collect {
                                        cvScoreBoard.text =
                                            getString(R.string.level, it.first, it.second)
                                    }
                            }
                        }
                    }

                    ScreenStates.LevelStartWarp -> {
                        transitionFromTo(
                            fromScene = levelCompleteScene.scene,
                            toScene = levelStartWarpScene.scene,
                            transition = Fade(Fade.MODE_IN).apply {
                                duration = 200
                            })
                        binding.root.setTrails(true)
                        levelStartWarpScene.binding.spaceShipView.animate()
                            .translationY(-binding.root.measuredHeight.toFloat())
                            .withEndAction {
                                binding.root.setTrails(false)
                                viewModel.updateUIState(ScreenStates.LevelStart)
                            }
                            .setDuration(4000L)
                            .start()
                    }

                    ScreenStates.GameOver -> {
                        gameOverScene.binding.apply {
                            tvScore.text =
                                getString(R.string.score_text, scoreFlow().value)

                            if (viewModel.previousState == ScreenStates.YouDied) {
                                tvDiedSubTitle.text = getString(R.string.enemyBreached)
                            } else {
                                val conserveAmmoText = getString(R.string.conserveAmmo)
                                tvDiedSubTitle.text =
                                    conserveAmmoText + getString(R.string.enemyBreached)
                            }

                            btnMainMenu.setOnClickListener {
                                viewModel.updateUIState(screenStates = ScreenStates.GameMenu)
                            }
                            btnTryAgain.setOnClickListener {
                                com.roy.data.LevelInfo.resetLevel()
                                viewModel.updateUIState(ScreenStates.LevelStart)
                            }
                        }
                        saveScore(lifecycleScope)
                        transitionFromTo(youDiedScene.scene,
                            gameOverScene.scene,
                            Fade(Fade.MODE_IN).apply {
                                duration = 600L
                                interpolator = AccelerateDecelerateInterpolator()
                            })
                    }

                    ScreenStates.YouDied -> {
                        val deathSoundManager = MediaPlayer.create(
                            this@observeScreenStates, R.raw.music_player_explosion
                        ).also {
                            it.setOnPreparedListener {
                                it.start()
                            }
                        }
                        delay(500)
                        transitionFromTo(
                            fromScene = gameScene.scene,
                            toScene = youDiedScene.scene,
                            transition = Fade(Fade.MODE_IN).apply {
                                addTarget(youDiedScene.binding.tvDiedText)
                                duration = 1600L
                            })
                        youDiedScene.binding.tvDiedText.animate().alpha(1F).scaleX(1.5F)
                            .scaleY(1.5F)
                            .setDuration(2200).withEndAction {
                                lifecycleScope.launchWhenCreated {
                                    delay(2000L)
                                    deathSoundManager.release()
                                    viewModel.updateUIState(ScreenStates.GameOver)
                                }
                            }.start()
                    }

                    ScreenStates.RanOutOfAmmo -> {
                        viewModel.updateUIState(ScreenStates.GameOver)
                    }
                }
            }
    }

    lifecycleScope.launchWhenCreated {
        delay(2000)
        viewModel.updateUIState(ScreenStates.GameMenu)
    }
}

private fun MainActivity.startLevelZero() {
    resetGameScene()
    levelZeroGameScene.binding.apply {
        val bulletStore = com.roy.data.BulletStore(com.roy.data.BulletStore.REFILL)

        enemyClusterView.bulletStore = bulletStore
        spaceShipView.bulletStore = bulletStore

        lifecycleScope.launch {
            scoreFlow().collect { score ->
                tvScore.text = getString(R.string.score_text, score)
            }
        }

        lifecycleScope.launch {
            bulletStore.bulletCountFlow().collect { ammoCount ->
                ammoCountView.setBulletCount(ammoCount = ammoCount, maxCount = bulletStore.maxCount)
            }
        }

        transitionFromTo(
            fromScene = levelStartScene.scene,
            toScene = levelZeroGameScene.scene,
            transition = transitionSet {
                addTransition(Slide(Gravity.BOTTOM)) {
                    duration = 1200
                    interpolator = AccelerateDecelerateInterpolator()
                    addTarget(spaceShipView)
                }
                onEnd {
                    showInitialInstructions(
                        levelZeroGameBinding = this@apply,
                        bulletStore = bulletStore
                    )
                }
            })
    }
}

fun startGame(binding: SceneGameBinding) {
    binding.spaceShipView.startGame()
    binding.enemyClusterView.startGame()
}

data class SceneContainer<Binding : ViewBinding>(
    val binding: Binding,
    val scene: Scene,
)
