package ru.srms.larp.platform.game.remote.action.result

import ru.srms.larp.platform.game.remote.action.Afforts
import ru.srms.larp.platform.game.remote.action.result.action.ResultAction
import ru.srms.larp.platform.game.remote.action.result.success.SuccessDetector
import ru.srms.larp.platform.game.remote.action.result.type.ResultProvider

/**
 * Created by Treble Snake on 13.10.2015.
 */
interface ResultHandler<T> extends Afforts {
  ResultAction<T> getAction()
  ResultProvider<T> getProvider()
  SuccessDetector getSuccessDetector()
  String getView()
  void calculate()
  String getResult()
}