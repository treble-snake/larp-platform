package ru.srms.larp.platform.game.remote.action.result.action

/**
 * Created by Treble Snake on 13.10.2015.
 */
interface ResultAction<T> {
  void onSuccess(Map params, T result)
  void onFailure(Map params, T result)
}