package ru.srms.larp.platform.game.remote

import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional
import ru.srms.larp.platform.RemotePlayerActionService
import ru.srms.larp.platform.game.character.GameCharacter
import ru.srms.larp.platform.game.remote.action.RemotePlayerAction

@Secured(['IS_AUTHENTICATED_REMEMBERED'])
@Transactional(readOnly = true)
class RemoteActionController {

  RemotePlayerActionService remotePlayerActionService


  def show(RemotePlayerActionData data) {
    RemotePlayerAction action = resolveAction(data)

    switch (action.currentState) {
      case RemotePlayerAction.State.NEW:
        render view: action.view, model: [data: data]
        break
      case RemotePlayerAction.State.IN_PROGRESS:
        redirect controller: 'remoteAction', action: 'process', id: data.id
        break
      default:
        redirect controller: 'remoteAction', action: 'result', id: data.id
        break
    }
  }

  def create() {
    RemotePlayerAction action = resolveAction()

    try {
      action.setCharacter(params.character as GameCharacter)
          .setCurrentState(RemotePlayerAction.State.NEW)
          .validate()
      render view: action.setupHandler.inputForm, model: []
    } catch (IllegalStateException | IllegalArgumentException e) {
      render view: 'create', model: [error: e.message]
    }
  }

  @Transactional
  def createGroup() {
    RemotePlayerAction action = resolveAction()
        .setCharacter(params.character as GameCharacter)
        .setCurrentState(RemotePlayerAction.State.NEW)
        .setGroup(new RemotePlayerActionsGroup())
    String form = null

    try {
      action.validate()

      form = action.setupHandler.inputForm
      action.setupHandler.setParameters(params)
          .validate()

      def data = remotePlayerActionService.saveActionData(action)
      redirect controller: 'remoteAction', action: 'show', id: data.id

    } catch (IllegalStateException | IllegalArgumentException e) {
      render view: form ?: 'create', model: [error: e.message]
    }
  }

  @Transactional
  def startGroup(RemotePlayerActionData ownerData) {
    ownerData.group.state = RemotePlayerActionsGroup.State.STARTED
    remotePlayerActionService.saveGroup(ownerData.group)
    redirect controller: 'remoteAction', action: 'show', id: ownerData.id
  }

  @Transactional
  def joinGroup(RemotePlayerActionData ownerData) {
    RemotePlayerAction ownerAction = resolveAction(ownerData)
    RemotePlayerAction action = resolveAction()

    try {
      action.with {
        character = params.character as GameCharacter
        currentState = State.NEW
        setupHandler.parameters = new HashMap<>(ownerAction.setupHandler.parameters)
        group = ownerAction.group
        validate()
      }

      if(params.containsKey('doJoinGroup')) {
        remotePlayerActionService.saveActionData(action)
        redirect controller: 'remoteAction', action: 'show'
      }

      render view: action.view, model: [action: action]
    } catch (IllegalStateException | IllegalArgumentException e) {
      render view: action.view, model: [error: e.message]
    }
  }

  @Transactional
  def start() {
    RemotePlayerAction action = resolveAction()
        .setCharacter(params.character as GameCharacter)
        .setCurrentState(RemotePlayerAction.State.NEW)
    String form = null

    try {
      action.validate()

      form = action.setupHandler.inputForm
      action.setupHandler.setParameters(params)
          .validate()

      action.changeState(RemotePlayerAction.State.IN_PROGRESS)
      def data = remotePlayerActionService.saveActionData(action)
      redirect controller: 'remoteAction', action: 'process', id: data.id

    } catch (IllegalStateException | IllegalArgumentException e) {
      render view: form ?: 'create', model: [error: e.message]
    }
  }

  @Transactional
  def process(RemotePlayerActionData data) {
    RemotePlayerAction action = resolveAction(data)

    try {
      action.validate()

      if(action.processHandler.complete) {
        action.changeState(RemotePlayerAction.State.COMPLETE)
        remotePlayerActionService.saveActionData(action, data)
        redirect controller: 'remoteAction', action: 'result', id: data.id
        return
      }

      String form = action.processHandler.inputForm
      render view: form, model: [data: data]

    } catch (IllegalStateException | IllegalArgumentException e) {
      render view: 'process', model: [error: e.message]
    }
  }

  @Transactional
  def iterate(RemotePlayerActionData data) {
    RemotePlayerAction action = resolveAction(data)

    try {
      action.validate()
      action.processHandler.iterate(params)
      remotePlayerActionService.saveActionData(action, data)
      redirect controller: 'remoteAction', action: 'process', id: data.id
    } catch (IllegalArgumentException e) {
      render view: 'process', model: [error: e.message]
    }
  }

  def result(RemotePlayerActionData data) {
    RemotePlayerAction action = resolveAction(data)

    try {
      action.validate()
      render view: action.resultHandler.view, model: [data: data]
    } catch (IllegalArgumentException e) {
      render view: 'result', model: [error: e.message]
    }

  }

  private RemotePlayerAction resolveAction(RemotePlayerActionData data = null) {
    RemotePlayerAction action
    if(data) remotePlayerActionService.loadAction(action, data)

    return action
  }
}
