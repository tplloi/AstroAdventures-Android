package com.roy.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.roy.data.DataStoreHelper.initDataStore
import com.roy.data.SoftBodyObject
import com.roy.data.SoftBodyObjectData
import com.roy.data.SoftBodyObjectType
import com.roy.databinding.AMainBinding
import com.roy.databinding.LevelStartSceneBinding
import com.roy.databinding.LevelStartWarpSceneBinding
import com.roy.databinding.LevelZeroGameBinding
import com.roy.databinding.MainMenuSceneBinding
import com.roy.databinding.SceneGameBinding
import com.roy.databinding.SceneGameInitBinding
import com.roy.databinding.SceneGameOverBinding
import com.roy.databinding.SceneHighScoresBinding
import com.roy.databinding.SceneLevelCompleteBinding
import com.roy.databinding.YouDiedSceneBinding
import com.roy.ui.game.views.bullets.BulletView
import com.roy.ui.game.views.enemyShip.EnemyDetailsCallback
import com.roy.ui.game.views.enemyShip.OnCollisionCallBack
import com.roy.ui.game.views.levelsZero.LevelZeroHelper
import com.roy.utils.BackgroundMusicManager
import com.roy.utils.goFullScreen
import kotlinx.coroutines.Job
import java.util.*

class MainActivity : AppCompatActivity(), SoftBodyObject.SoftBodyObjectTracker, OnCollisionCallBack,
    EnemyDetailsCallback, LevelZeroHelper {

    lateinit var binding: AMainBinding

    val viewModel by lazy {
        ViewModelProvider(
            owner = this,
            factory = ViewModelProvider.NewInstanceFactory()
        )[MainViewModel::class.java]
    }

    lateinit var initScene: SceneContainer<SceneGameInitBinding>
    lateinit var levelCompleteScene: SceneContainer<SceneLevelCompleteBinding>
    lateinit var levelZeroGameScene: SceneContainer<LevelZeroGameBinding>
    lateinit var levelStartWarpScene: SceneContainer<LevelStartWarpSceneBinding>
    lateinit var gameMenuScene: SceneContainer<MainMenuSceneBinding>
    lateinit var youDiedScene: SceneContainer<YouDiedSceneBinding>
    lateinit var gameScene: SceneContainer<SceneGameBinding>
    lateinit var levelStartScene: SceneContainer<LevelStartSceneBinding>
    lateinit var gameOverScene: SceneContainer<SceneGameOverBinding>
    lateinit var highScoreScene: SceneContainer<SceneHighScoresBinding>
    val backgroundMusicManager by lazy {
        BackgroundMusicManager(applicationContext).apply {
            lifecycle.addObserver(this)
        }
    }
    private val transitionManager by lazy { TransitionManager() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        goFullScreen()
        initDataStore(this)
        binding = AMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            binding.flRootContainer.setPadding(
                /* left = */ 0,
                /* top = */ insets.systemWindowInsetTop,
                /* right = */ 0,
                /* bottom = */ insets.systemWindowInsetBottom
            )
            insets
        }
        initScenes()
        observeScreenStates()
    }

    private fun initScenes() {
        initScene = SceneGameInitBinding.inflate(layoutInflater, binding.root, false).let {
            SceneContainer(
                binding = it,
                scene = Scene(binding.flRootContainer, it.root)
            )
        }

        gameMenuScene = MainMenuSceneBinding.inflate(layoutInflater, binding.root, false).let {
            SceneContainer(
                binding = it,
                scene = Scene(binding.flRootContainer, it.root)
            )
        }

        levelStartScene =
            LevelStartSceneBinding.inflate(layoutInflater, binding.root, false).let {
                SceneContainer(
                    binding = it,
                    scene = Scene(binding.flRootContainer, it.root)
                )
            }

        youDiedScene =
            YouDiedSceneBinding.inflate(layoutInflater, binding.root, false).let {
                SceneContainer(
                    binding = it,
                    scene = Scene(binding.flRootContainer, it.root)
                )
            }

        highScoreScene =
            SceneHighScoresBinding.inflate(layoutInflater, binding.root, false).let {
                SceneContainer(
                    binding = it,
                    scene = Scene(binding.flRootContainer, it.root)
                )
            }

        resetGameScene()

        gameOverScene =
            SceneGameOverBinding.inflate(layoutInflater, binding.root, false).let {
                SceneContainer(
                    binding = it,
                    scene = Scene(binding.flRootContainer, it.root)
                )
            }
    }

    override fun initBulletTracking(softBodyObjectData: SoftBodyObjectData) {
        if (softBodyObjectData.sender == BulletView.Sender.PLAYER) {
            gameScene.binding.enemyClusterView.checkCollision(softBodyObjectData)
        } else {
            gameScene.binding.spaceShipView.checkCollision(softBodyObjectData)
        }
    }

    override fun cancelTracking(
        bulletId: UUID,
        sender: BulletView.Sender,
    ) {
        if (sender == BulletView.Sender.PLAYER) {
            gameScene.binding.spaceShipView.removeSoftBodyEntry(bulletId)
        } else {
            gameScene.binding.enemyClusterView.removeSoftBodyEntry(bulletId)
        }
    }

    override fun onCollision(softBodyObject: SoftBodyObjectData) {
        if (softBodyObject.objectType == SoftBodyObjectType.BULLET) {
            gameScene.binding.bulletView.destroyBullet(softBodyObject.objectId)
        } else {
            gameScene.binding.dropsView.destroyObject(softBodyObject.objectId)
        }
    }

    override fun onAllEliminated(ammoCount: Int) {
        showLevelCompleteScene(ammoCount)
    }

    internal fun showLevelCompleteScene(ammoCount: Int) {
        viewModel.updateUIState(ScreenStates.LevelComplete(ammoCount))
    }

    override fun onCanonReady(
        enemyX: Float,
        enemyY: Float,
    ) {
        gameScene.binding.bulletView.fire(
            x = enemyX,
            y = enemyY,
            sender = BulletView.Sender.ENEMY
        )
    }

    override fun hasDrop(
        enemyX: Float,
        enemyY: Float,
    ) {
        gameScene.binding.dropsView.dropGift(x = enemyX, y = enemyY)
    }

    fun resetGameScene() {
        binding.flRootContainer.removeAllViews()
        levelCompleteScene =
            SceneLevelCompleteBinding.inflate(layoutInflater, binding.root, false).let {
                SceneContainer(
                    binding = it,
                    scene = Scene(binding.flRootContainer, it.root)
                )
            }

        levelZeroGameScene =
            LevelZeroGameBinding.inflate(layoutInflater, binding.root, false).let {
                SceneContainer(
                    binding = it,
                    scene = Scene(binding.flRootContainer, it.root)
                )
            }

        levelStartWarpScene =
            LevelStartWarpSceneBinding.inflate(layoutInflater, binding.root, false).let {
                SceneContainer(
                    binding = it,
                    scene = Scene(binding.flRootContainer, it.root)
                )
            }

        gameScene = SceneGameBinding.inflate(layoutInflater, binding.root, false).let {
            SceneContainer(
                binding = it,
                scene = Scene(binding.flRootContainer, it.root)
            )
        }.apply {
            binding.apply {
                bulletView.softBodyObjectTracker = this@MainActivity
                dropsView.softBodyObjectTracker = this@MainActivity
                healthView.onHealthEmpty = {
                    viewModel.updateUIState(ScreenStates.YouDied)
                }
                enemyClusterView.enemyDetailsCallback = this@MainActivity
                enemyClusterView.onCollisionCallBack = this@MainActivity
                spaceShipView.onCollisionCallBack = this@MainActivity
            }
        }
    }

    override fun onGameOver() {
        viewModel.updateUIState(ScreenStates.YouDied)
    }

    fun transitionFromTo(
        fromScene: Scene,
        toScene: Scene,
        transition: Transition,
    ) {
        transitionManager.setTransition(
            /* fromScene = */ fromScene,
            /* toScene = */toScene,
            /* transition = */transition
        )
        transitionManager.transitionTo(toScene)
    }

    fun transitionTo(
        toScene: Scene,
        transition: Transition,
    ) {
        transitionManager.setTransition(
            /* scene = */ toScene,
            /* transition = */ transition
        )
        transitionManager.transitionTo(toScene)
    }

    //TODO migrate onBackPressed
    override fun onBackPressed() {
        when (viewModel.observeScreenState().value) {
            ScreenStates.GameMenu -> finish()
            else -> viewModel.updateUIState(ScreenStates.GameMenu)
        }
    }

    internal var uiEventJob: Job = Job()

    override fun onDestroy() {
        super.onDestroy()
        uiEventJob.cancel()
    }
}
