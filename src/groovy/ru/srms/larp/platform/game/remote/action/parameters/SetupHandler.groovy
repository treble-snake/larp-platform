package ru.srms.larp.platform.game.remote.action.parameters

/**
 * Created by Treble Snake on 13.10.2015.
 */
interface SetupHandler {
  String getInputForm()

  SetupHandler setParameters(Map inputParameters)

  Map getParameters()

  void validate() throws IllegalArgumentException
}