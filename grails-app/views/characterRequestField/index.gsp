<%@ page import="ru.srms.larp.platform.game.roles.GameRole; ru.srms.larp.platform.game.character.request.RequestFormField" %>
<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="mainWithActions">
  <g:set var="subject" value="${requestFormFieldInstanceList as List<RequestFormField>}"/>
  <g:set var="title" value="Поля анкеты игрока"/>
  <title>${title}</title>
</head>

<body>

<content tag="actions">

  <g:if test="${params.role?.id}">
    <ingame:link class="item" controller="gameRole" action="index">
      <i class="arrow left grey icon"></i> Назад</ingame:link>
  </g:if>
  <g:else>
  <ingame:link mapping="gameRequest" class="item" action="index" controller="characterRequest">
    <i class="arrow left grey icon"></i> Назад</ingame:link>
  </g:else>

  <ingame:link class="item" action="create"
               params="${params.role?.id ? ['role.id': params.role?.id] : [:]}"><i class="add green icon"></i> Добавить</ingame:link>
</content>

<content tag="content">

  <g:if test="${params.role?.id && !subject}">
    <g:set var="isAvailable" value="${GameRole.get(params.role.id)?.requestAvailable}"/>
    <ingame:link class="ui blue ${isAvailable ? '' : 'basic'} button"
        controller="gameRole" action="toggleRequestAvailable" id="${params.role.id}">
        ${isAvailable ? 'Не о' : 'О'}тображать на форме анкеты
    </ingame:link>
  </g:if>

  <table class="ui celled padded very basic table">
    <thead>
    <tr>
      <th>Название</th>
      <th>Действия</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${subject}" var="item">
      <tr>
        <td><ingame:link action="show" id="${item.id}">${item.title}</ingame:link></td>
        <td>
          <ingame:link action="edit" id="${item.id}" class="ui yellow icon basic button"
                       title="Редактировать"><i class="yellow edit icon"></i></ingame:link>
          <ingame:link action="delete" id="${item.id}" class="ui red icon basic button"
                       title="Удалить" onclick="return confirm('Вы уверены?');">
            <i class="red delete icon"></i></ingame:link>
        </td>
      </tr>
    </g:each>
    </tbody>
    <g:render template="/shared/semantic/tablePaginate"
              model="[colspan: 2, itemsQty: itemsQty]"/>
  </table>

</content>
</body>
</html>
