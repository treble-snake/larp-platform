package ru.srms.larp.platform.game.remote.action.result.type

/**
 * Created by Treble Snake on 13.10.2015.
 */
interface ResultProvider<T> {
  T getResult(Map params)
}