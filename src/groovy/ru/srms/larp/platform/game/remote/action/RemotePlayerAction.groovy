package ru.srms.larp.platform.game.remote.action

import ru.srms.larp.platform.game.character.GameCharacter
import ru.srms.larp.platform.game.remote.RemotePlayerActionData
import ru.srms.larp.platform.game.remote.RemotePlayerActionsGroup
import ru.srms.larp.platform.game.remote.action.parameters.SetupHandler
import ru.srms.larp.platform.game.remote.action.process.ProcessHandler
import ru.srms.larp.platform.game.remote.action.result.ResultHandler

/**
 * Created by Treble Snake on 13.10.2015.
 */
interface RemotePlayerAction {

  enum State {
    NEW, IN_PROGRESS, COMPLETE, SUCCESS, FAILURE, CANCELED
  }

  RemotePlayerAction initialize(RemotePlayerActionData data) throws IllegalArgumentException

  RemotePlayerAction changeState(State newState) throws IllegalStateException

  RemotePlayerAction validate() throws IllegalStateException, IllegalArgumentException

  RemotePlayerAction setCharacter(GameCharacter character)

  GameCharacter getCharacter()

  RemotePlayerAction setCurrentState(State state)

  State getCurrentState()

  RemotePlayerActionsGroup getGroup()

  RemotePlayerAction setGroup(RemotePlayerActionsGroup group)

  String getView()

  boolean isPartOfGroup()

  boolean isParallelAllowed()

  boolean isGroupActionAllowed()

  SetupHandler getSetupHandler()

  ProcessHandler getProcessHandler()

  ResultHandler getResultHandler()
}