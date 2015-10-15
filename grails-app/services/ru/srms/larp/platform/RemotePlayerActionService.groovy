package ru.srms.larp.platform

import grails.transaction.Transactional
import ru.srms.larp.platform.game.remote.RemotePlayerActionData
import ru.srms.larp.platform.game.remote.RemotePlayerActionsGroup
import ru.srms.larp.platform.game.remote.action.RemotePlayerAction

@Transactional(readOnly = true)
class RemotePlayerActionService {

  @Transactional
  RemotePlayerActionData saveActionData(RemotePlayerAction action, RemotePlayerActionData data = null) {
    data = data ?: new RemotePlayerActionData()

    if (action.setupHandler.parameters) {
      def model = action.setupHandler.parameters
      model.save(flush: true)
      if (!data.inputParameters)
        data.inputParameters = EntityWrapper.wrap(model)
    }

    if (action.processHandler.parameters) {
      def model = action.processHandler.parameters
      model.save(flush: true)
      if (!data.processParameters)
        data.processParameters = EntityWrapper.wrap(model)
    }

    data.with {
      character = action.character
      state = action.currentState
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
