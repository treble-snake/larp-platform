<%@ page import="ru.srms.larp.platform.game.mail.MailBox; org.springframework.validation.FieldError" %>
<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="mainWithActions">
  <g:set var="subject" value="${mailBoxInstance as MailBox}"/>
  <g:set var="title" value="Создание почтового ящика"/>
  <title>${title}</title>
</head>

<body>

<content tag="actions">
  <ingame:link class="item" action="index"><i class="cancel grey icon"></i> Отмена</ingame:link>
</content>

<content tag="content">
  <g:render template="/shared/fromErrors" bean="${subject}" var="subject"/>
  <ingame:form class="ui form" url="[resource: subject, action: 'save']">
    <div class="ui three fields">
      <g:render template="form"/>
    </div>
    <ui:submit icon="add">Сохранить</ui:submit>
  </ingame:form>
</content>

</body>
</html>
