package ru.srms.larp.platform.game.remote

import ru.srms.larp.platform.EntityWrapper
import ru.srms.larp.platform.game.Game
import ru.srms.larp.platform.game.InGameStuff
import ru.srms.larp.platform.game.character.GameCharacter
import ru.srms.larp.platform.game.remote.action.RemoteActionParameters
import ru.srms.larp.platform.game.remote.action.RemotePlayerAction

class RemotePlayerActionData implements InGameStuff {

  // TODO - add type of the action: Hacking, Craft, etc

  GameCharacter character
  RemotePlayerAction.State state
  EntityWrapper<? extends RemoteActionParameters> inputParameters
  EntityWrapper<? extends RemoteActionParameters> processParameters

  String result

  static belongsTo = [group: RemotePlayerActionsGroup]

  static constraints = {
    group nullable: true
    inputParameters nullable: true
    processParameters nullable: true
    result nullable: true, maxSize: 4096
  }

  @Override
  Game extractGame() {
    return character.game
  }
}
