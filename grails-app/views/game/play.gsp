<%@ page import="ru.srms.larp.platform.game.character.GameCharacter; ru.srms.larp.platform.game.Game" %>
<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="mainWithActions">
  <g:set var="subject" value="${gameInstance as Game}"/>
  <g:set var="title" value="${subject.title}"/>
  <title>${title}</title>
</head>

<body>

<content tag="actions">
  <g:link class="item" action="create"><i
      class="add green icon"></i> Создать игру</g:link>
  <sec:permitted object="${subject}" permission="administration">
    <tmpl:gmControls game="${subject}"/>
  </sec:permitted>
  <sec:ifAllGranted roles="ROLE_ADMIN">
    <g:link class="item" action="delete" resource="${subject}"
            onclick="return confirm('Вы уверены?');"><i
        class="delete red icon"></i> Удалить</g:link>
  </sec:ifAllGranted>
</content>B

<content tag="content">
  <g:if test="${params.game.active}">
    <div class="ui green label">Игра идет</div>
  </g:if>
  <g:else>
    <div class="ui red label">Игра приостановлена</div>
  </g:else>
  <div class="ui hidden divider"></div>

  <div class="ui two columns stackable grid">

    <div class="ten wide column">
      <section class="ui pilled segment">
        <div class="ui green ribbon label">Об игре</div>

        <div class="ui small hidden divider"></div>

        <div class="content richText"><hc:cleanHtml unsafe="${subject.overview}"
                                                    whitelist="rich-text"/></div>
      </section>
    </div>

    <div class="six wide column">

      <section class="ui pilled segment">
        <div class="ui violet ribbon label">Ваши персонажи</div>

        <g:if test="${characters}">
          <g:each in="${characters}" var="item">
            <g:set var="c" value="${item as GameCharacter}"/>
            <g:link mapping="playAs" params="[gameAlias: subject.alias, charAlias: c.alias]"
                    class="ui card">
              <div class="content">
                <div class="header">${c.name}</div>

                <div class="meta">
                  <g:each in="${c.roles}" var="role" status="i">
                    ${role.title}
                    <g:if test="${i < c.roles.size() - 1}">,</g:if>
                  </g:each>
                </div>
              </div>

              <div class="extra content">
                <div class="right floated">
                  <!-- TODO integrate with real mail -->
                  0 <i class="outline mail icon"></i>
                </div>
              </div>
            </g:link>
          </g:each>
        </g:if>
        <g:else>
          <ui:message type="info">Увы, в этой игре у вас пока нет персонажей.</ui:message>
        </g:else>
      </section>
      <g:if test="${params.game.modules.contains(Game.GameModule.REQUEST_FORM)}">
        <section class="ui pilled segment">
          <div class="ui olive ribbon label">Ваши заявки</div>
          <g:if test="${requests}">
            <div class="ui middle aligned relaxed divided selection list">
              <g:each in="${requests}" var="item">
                <g:render template="request" model="[item: item]"/>
              </g:each>
            </div>
          </g:if>
          <g:else>
            <ui:message
                type="info">Вы еще не подавали заявок на участие в этой игре.</ui:message>
          </g:else>
          <ingame:link mapping="gameRequest" class="ui button" controller="characterRequest"
                       action="create">Создать заявку на участие</ingame:link>
        </section>
      </g:if>
      <section class="ui pilled segment">
        <div class="ui red ribbon label">Мастера</div>

        <div class="ui selection list">
          <g:each in="${subject.masters}" var="m">
            <g:link class="labeled icon item" controller="account" id="${m.id}">
              <div class="header"><i class="user icon"></i> ${m.name}</div>
            </g:link>
          </g:each>
        </div>
      </section>
    </div>

  </div>
</content>
</body>
</html>
