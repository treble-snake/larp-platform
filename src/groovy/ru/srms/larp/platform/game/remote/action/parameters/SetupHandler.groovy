package ru.srms.larp.platform.game.remote.action.parameters

import ru.srms.larp.platform.game.remote.action.RemoteActionParameters

/**
 * Created by Treble Snake on 13.10.2015.
 */
interface SetupHandler<P extends RemoteActionParameters> {
  String getInputForm()

  SetupHandler collectParameters(Map inputParameters)

  SetupHandler setParameters(P inputParameters)

  P getParameters()

  void validate() throws IllegalArgumentException
}