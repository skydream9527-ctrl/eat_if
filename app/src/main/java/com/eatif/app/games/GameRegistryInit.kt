package com.eatif.app.games

import com.eatif.app.games.boxpusher.BoxPusherGame
import com.eatif.app.games.climb100.Climb100Game
import com.eatif.app.games.flappy.FlappyEatGame
import com.eatif.app.games.game2048.Game2048
import com.eatif.app.games.jump.JumpGame
import com.eatif.app.games.minesweeper.MinesweeperGame
import com.eatif.app.games.needle.NeedleGame
import com.eatif.app.games.onetstroke.OneStrokeGame
import com.eatif.app.games.rps.RockPaperScissorsGame
import com.eatif.app.games.runner.InfiniteRunnerGame
import com.eatif.app.games.shooting.ShootingGame
import com.eatif.app.games.slot.SlotMachineGame
import com.eatif.app.games.snake.SnakeGame
import com.eatif.app.games.spinwheel.SpinWheelGame
import com.eatif.app.games.tetris.TetrisGame

fun initGameRegistry() {
    GameRegistry.register(GameConfig("spinwheel") { foods, isPaused, _, onResult, mode ->
        SpinWheelGame(foods = foods, isPaused = isPaused, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("rps") { foods, isPaused, _, onResult, mode ->
        RockPaperScissorsGame(foods = foods, isPaused = isPaused, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("slot") { foods, isPaused, _, onResult, mode ->
        SlotMachineGame(foods = foods, isPaused = isPaused, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("needle") { foods, isPaused, _, onResult, mode ->
        NeedleGame(foods = foods, isPaused = isPaused, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("jump") { foods, isPaused, _, onResult, mode ->
        JumpGame(foods = foods, isPaused = isPaused, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("climb100") { foods, isPaused, _, onResult, mode ->
        Climb100Game(foods = foods, isPaused = isPaused, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("2048") { foods, isPaused, _, onResult, mode ->
        Game2048(foods = foods, isPaused = isPaused, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("snake", supportsSelfPause = true) { foods, isPaused, onPauseToggle, onResult, mode ->
        SnakeGame(foods = foods, isPaused = isPaused, onPauseToggle = onPauseToggle, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("tetris", supportsSelfPause = true) { foods, isPaused, onPauseToggle, onResult, mode ->
        TetrisGame(foods = foods, isPaused = isPaused, onPauseToggle = onPauseToggle, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("minesweeper") { foods, isPaused, _, onResult, mode ->
        MinesweeperGame(foods = foods, isPaused = isPaused, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("onetstroke") { foods, isPaused, _, onResult, mode ->
        OneStrokeGame(foods = foods, isPaused = isPaused, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("flappy", supportsSelfPause = true) { foods, isPaused, onPauseToggle, onResult, mode ->
        FlappyEatGame(foods = foods, isPaused = isPaused, onPauseToggle = onPauseToggle, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("boxpusher") { foods, isPaused, _, onResult, mode ->
        BoxPusherGame(foods = foods, isPaused = isPaused, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("runner", supportsSelfPause = true) { foods, isPaused, onPauseToggle, onResult, mode ->
        InfiniteRunnerGame(foods = foods, isPaused = isPaused, onPauseToggle = onPauseToggle, onResult = onResult, mode = mode)
    })
    GameRegistry.register(GameConfig("shooting", supportsSelfPause = true) { foods, isPaused, onPauseToggle, onResult, mode ->
        ShootingGame(foods = foods, isPaused = isPaused, onPauseToggle = onPauseToggle, onResult = onResult, mode = mode)
    })
}
