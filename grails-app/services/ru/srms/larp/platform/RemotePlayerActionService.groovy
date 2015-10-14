package ru.srms.larp.platform

import grails.converters.JSON
import grails.transaction.Transactional
import ru.srms.larp.platform.game.remote.RemotePlayerActionData
import ru.srms.larp.platform.game.remote.RemotePlayerActionsGroup
import ru.srms.larp.platform.game.remote.action.RemotePlayerAction

@Transactional(readOnly = true)
class RemotePlayerActionService {

  @Transactional
  RemotePlayerActionData saveActionData(RemotePlayerAction action, RemotePlayerActionData data = null) {
    data = data ?: new RemotePlayerActionData()

    data.with {
      character = action.character
      state = action.currentState
      // TODO check serialization to JSON
      inputParameters = action.setupHandler.parameters as JSON
      processParameters = action.processHandler.parameters as JSON
      result = action.resultHandler.result

      save(flush: true)
    }

    if(action.group)
      addToGroup(action.group, data)

    return data
  }

  private void addToGroup(RemotePlayerActionsGroup group, RemotePlayerActionData data) {
    if (!group.id)
      group.owner = data

    if(!group.actions.collect {it.id}.contains(data.id))
      group.addToActions(data)

    group.save()
  }

  @Transactional
  void saveGroup(RemotePlayerActionsGroup group) {
    group.save()
  }

  RemotePlayerAction loadAction(RemotePlayerAction action, RemotePlayerActionData data) {
    action.initialize(data)
  }
}
