package ru.srms.larp.platform.game.remote

import ru.srms.larp.platform.game.Game
import ru.srms.larp.platform.game.InGameStuff
import ru.srms.larp.platform.game.character.GameCharacter
import ru.srms.larp.platform.game.remote.action.RemotePlayerAction

class RemotePlayerActionData implements InGameStuff {

  // TODO - add type of the action: Hacking, Craft, etc

  GameCharacter character
  RemotePlayerAction.State state
  String inputParameters
  String processParameters
  String result

  static belongsTo = [group: RemotePlayerActionsGroup]

  static constraints = {
    group nullable: true
    inputParameters nullable: true, maxSize: 4096
    processParameters nullable: true, maxSize: 4096
    result nullable: true, maxSize: 4096
  }

  @Override
  Game extractGame() {
    return character.game
  }
}
