package ru.srms.larp.platform.game.remote

import ru.srms.larp.platform.game.Game
import ru.srms.larp.platform.game.InGameStuff

class RemotePlayerActionsGroup implements InGameStuff {

  RemotePlayerActionData owner
  State state = State.GATHERING
  static hasMany = [actions: RemotePlayerActionData]

  static constraints = {
    actions minSize: 1
  }

  @Override
  Game extractGame() {
    return owner.character.game
  }

  enum State {
    GATHERING, READY, COMPLETE
  }
}
