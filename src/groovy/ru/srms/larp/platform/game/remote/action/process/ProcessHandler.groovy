package ru.srms.larp.platform.game.remote.action.process

import ru.srms.larp.platform.game.remote.action.Afforts

/**
 * Created by Treble Snake on 13.10.2015.
 */
interface ProcessHandler extends Afforts {
  boolean isComplete()

  String getInputForm()

  Map getParameters()

  ProcessHandler setParameters(Map processParameters)

  void iterate(Map params) throws IllegalArgumentException
}