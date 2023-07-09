package com.roy.ui.game.views.levelsZero

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.roy.R
import com.roy.data.DataStoreHelper.setHasCompletedTutorial
import com.roy.data.LevelInfo
import com.roy.data.SoftBodyObject
import com.roy.data.SoftBodyObjectData
import com.roy.data.SoftBodyObjectType
import com.roy.databinding.SceneGameLevelZeroBinding
import com.roy.ui.MainActivity
import com.roy.ui.ScreenStates
import com.roy.ui.game.views.bullets.BulletView
import com.roy.ui.game.views.bullets.LevelZeroCallBackBullet
import com.roy.ui.game.views.enemyShip.EnemyDetailsCallback
import com.roy.ui.game.views.enemyShip.OnCollisionCallBack
import com.roy.ui.game.views.instructions.DialogHelper
import com.roy.ui.game.views.playersShip.LevelZeroCallBackPlayer
import com.roy.utils.PLAYER_BULLET_SOUND
import com.roy.utils.SoundData
import com.roy.utils.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

interface LevelZeroHelper {

    fun MainActivity.showInitialInstructions(
        levelZeroGameBinding: SceneGameLevelZeroBinding,
        bulletStore: com.roy.data.BulletStore,
    ) {
        var dialogJob: Job = Job()

        levelZeroGameBinding.apply {

            val itemSoundManger = SoundManager(
                applicationContext,
                SoundData(
                    R.raw.player_bullet_sound,
                    PLAYER_BULLET_SOUND
                ),
                lifecycle
            )

            dropsView.setSoundManager(itemSoundManger)
            bulletView.setSoundManager(itemSoundManger)

            val dialogHelper = DialogHelper()

            suspend fun SceneGameLevelZeroBinding.handleDialogs() {
                val nextDialog = dialogHelper.getNextDialog()
                nextDialog?.let { dialog ->
                    when (dialog.type) {
                        DialogHelper.InstructionType.TEXT -> {
                            dialogView.alpha = 1F
                            tvBlinking.stopBlinking()
                            dialogView.addDialog(dialog.text)
                            delay(dialog.duration)
                        }
                        DialogHelper.InstructionType.AMMO -> {
                            dialogView.addDialog(dialog.text)
                            ammoCountView.isVisible = true
                            delay(dialog.duration)
                        }
                        DialogHelper.InstructionType.HEALTH -> {
                            dialogView.addDialog(dialog.text)
                            healthView.isVisible = true
                            delay(dialog.duration)
                        }
                        DialogHelper.InstructionType.AmmoWarning -> {
                            dialogHelper.setLock()
                            dialogView.addDialog(dialog.text)
                            delay(dialog.duration)
                            dialogHelper.removeLock()
                        }
                        DialogHelper.InstructionType.TILT -> {
                            dialogHelper.setLock()
                            dialogView.isInvisible = true
                            spaceShipView.levelZeroCallBackPlayer =
                                object : LevelZeroCallBackPlayer {
                                    override fun onTilted() {
                                        dialogHelper.removeLock()
                                        root.post {
                                            dialogView.performClick()
                                        }
                                    }
                                }
                            spaceShipView.startGame()
                            tvBlinking.text = dialog.text
                            tvBlinking.startBlinking()
                        }
                        DialogHelper.InstructionType.FIRE -> {
                            dialogHelper.setLock()
                            dialogView.isInvisible = true
                            tvBlinking.text = dialog.text
                            bulletView.levelZeroCallBackBullet = object : LevelZeroCallBackBullet {
                                override fun onFired() {
                                    dialogHelper.removeLock()
                                    root.post {
                                        dialogView.performClick()
                                    }
                                }
                            }
                            bulletView.setOnClickListener {
                                if (bulletStore.getAmmoCount() != 0) {
                                    bulletStore.updateInventory()
                                    bulletView.fire(spaceShipView.getShipX(),
                                        spaceShipView.getShipY(),
                                        BulletView.Sender.PLAYER)
                                } else {
                                    viewModel.updateUIState(ScreenStates.RanOutOfAmmo)
                                }
                            }
                            bulletView.isVisible = true
                            tvBlinking.startBlinking()
                        }
                        DialogHelper.InstructionType.EnemySpotted -> {
                            dialogHelper.setLock()
                            enemyClusterView.disableInit = true
                            enemyClusterView.animate()
                                .translationYBy((resources.getDimension(R.dimen.enemyTranslateY)))
                                .setDuration(2000L)
                                .withEndAction {
                                    dialogHelper.removeLock()
                                    dialogView.performClick()
                                }.start()
                        }
                        DialogHelper.InstructionType.EnemyTranslated -> {
                            dialogHelper.setLock()
                            dialogView.addDialog(dialog.text)
                            initUIComponents(levelZeroGameBinding,
                                this@showInitialInstructions)
                            tvScore.isVisible = true
                            LevelInfo.hasPlayedTutorial = true
                            setHasCompletedTutorial()
                            delay(dialog.duration)
                            TransitionManager.beginDelayedTransition(root)
                            dialogView.isVisible = false
                            enemyClusterView.startGame()
                            return
                        }
                    }
                }
            }

            fun startLoopingThroughDialogs() {
                dialogJob.cancel()
                dialogJob = lifecycleScope.launch {
                    (dialogHelper.counter..dialogHelper.getItemSize()).forEach { _ ->
                        handleDialogs()
                    }
                }
            }

            dialogView.setOnClickListener {
                if (dialogHelper.isOpen)
                    startLoopingThroughDialogs()
            }

            startLoopingThroughDialogs()
        }
    }

    fun MainActivity.initUIComponents(
        levelZeroGameBinding: SceneGameLevelZeroBinding,
        mainActivity: MainActivity,
    ) {
        levelZeroGameBinding.guideline.updateLayoutParams<ConstraintLayout.LayoutParams> {
            guidePercent = 0.88F
        }
        levelZeroGameBinding.healthView.onHealthEmpty = {
            mainActivity.viewModel.updateUIState(ScreenStates.YouDied)
        }
        val softBodyObjectTracker = object : SoftBodyObject.SoftBodyObjectTracker {
            override fun initBulletTracking(softBodyObjectData: SoftBodyObjectData) {
                if (softBodyObjectData.sender == BulletView.Sender.PLAYER) {
                    levelZeroGameBinding.enemyClusterView.checkCollision(softBodyObjectData)
                } else {
                    levelZeroGameBinding.spaceShipView.checkCollision(softBodyObjectData)
                }
            }

            override fun cancelTracking(bulletId: UUID, sender: BulletView.Sender) {
                if (sender == BulletView.Sender.PLAYER) {
                    levelZeroGameBinding.spaceShipView.removeSoftBodyEntry(bulletId)
                } else {
                    levelZeroGameBinding.enemyClusterView.removeSoftBodyEntry(bulletId)
                }
            }

        }
        val onCollisionCallBack = object : OnCollisionCallBack {
            override fun onCollision(softBodyObject: SoftBodyObjectData) {
                if (softBodyObject.objectType == SoftBodyObjectType.BULLET) {
                    levelZeroGameBinding.bulletView.destroyBullet(softBodyObject.objectId)
                } else {
                    levelZeroGameBinding.dropsView.destroyObject(softBodyObject.objectId)
                }
            }

        }

        val enemyDetailsCallback = object : EnemyDetailsCallback {
            override fun onAllEliminated(ammoCount: Int) {
                showLevelCompleteScene(ammoCount)
            }

            override fun onCanonReady(enemyX: Float, enemyY: Float) {
                levelZeroGameBinding.bulletView.fire(enemyX, enemyY, BulletView.Sender.ENEMY)
            }

            override fun hasDrop(enemyX: Float, enemyY: Float) {
                levelZeroGameBinding.dropsView.dropGift(enemyX, enemyY)
            }

            override fun onGameOver() {
                viewModel.updateUIState(ScreenStates.YouDied)
            }
        }
        levelZeroGameBinding.apply {
            bulletView.softBodyObjectTracker = softBodyObjectTracker
            dropsView.softBodyObjectTracker = softBodyObjectTracker
            enemyClusterView.enemyDetailsCallback = enemyDetailsCallback
            enemyClusterView.onCollisionCallBack = onCollisionCallBack
            spaceShipView.onCollisionCallBack = onCollisionCallBack
        }
    }
}