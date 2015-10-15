package ru.srms.larp.platform.game.remote.action.process

import ru.srms.larp.platform.game.remote.action.Afforts
import ru.srms.larp.platform.game.remote.action.RemoteActionParameters

/**
 * Created by Treble Snake on 13.10.2015.
 */
interface ProcessHandler<P extends RemoteActionParameters> extends Afforts {
  boolean isComplete()

  String getInputForm()

  P getParameters()

  ProcessHandler setParameters(P processParameters)

  void iterate(Map params) throws IllegalArgumentException
}