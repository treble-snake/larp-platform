<%@ page import="ru.srms.larp.platform.game.Game" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'game.label', default: 'Game')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-game" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                           default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message
                code="default.home.label"/></a></li>
        <li><g:link class="list" action="index"><g:message code="default.list.label"
                                                           args="[entityName]"/></g:link></li>

        <sec:ifLoggedIn>
            <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
        </sec:ifLoggedIn>
    </ul>
</div>

<div id="show-game" class="content scaffold-show" role="main">
    <h1><g:message code="default.show.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <ol class="property-list game">

        <g:if test="${gameInstance?.alias}">
            <li class="fieldcontain">
                <span id="alias-label" class="property-label"><g:message code="game.alias.label"
                                                                         default="Alias"/></span>

                <span class="property-value" aria-labelledby="alias-label"><g:fieldValue
                        bean="${gameInstance}" field="alias"/></span>

            </li>
        </g:if>

        <g:if test="${gameInstance?.overview}">
            <li class="fieldcontain">
                <span id="overview-label" class="property-label"><g:message
                        code="game.overview.label" default="Overview"/></span>

                <span class="property-value" aria-labelledby="overview-label"><g:fieldValue
                        bean="${gameInstance}" field="overview"/></span>

            </li>
        </g:if>

        <g:if test="${gameInstance?.characters}">
            <li class="fieldcontain">
                <span id="characters-label" class="property-label"><g:message
                        code="game.characters.label" default="Characters"/></span>

                <g:each in="${gameInstance.characters}" var="c">
                    <span class="property-value" aria-labelledby="characters-label"><g:link
                            controller="gameCharacter" action="show"
                            id="${c.id}">${c?.encodeAsHTML()}</g:link></span>
                </g:each>

            </li>
        </g:if>

        <g:if test="${gameInstance?.masters}">
            <li class="fieldcontain">
                <span id="masters-label" class="property-label"><g:message code="game.masters.label"
                                                                           default="Masters"/></span>

                <g:each in="${gameInstance.masters}" var="m">
                    <span class="property-value" aria-labelledby="masters-label"><g:link
                            controller="springUser" action="show"
                            id="${m.id}">${m?.encodeAsHTML()}</g:link></span>
                </g:each>

            </li>
        </g:if>

        <g:if test="${gameInstance?.title}">
            <li class="fieldcontain">
                <span id="title-label" class="property-label"><g:message code="game.title.label"
                                                                         default="Title"/></span>

                <span class="property-value" aria-labelledby="title-label"><g:fieldValue
                        bean="${gameInstance}" field="title"/></span>

            </li>
        </g:if>

    </ol>
        <fieldset class="buttons">
            <sec:permitted object="${gameInstance}" permission="administration">
                <g:link class="edit" action="edit" resource="${gameInstance}"><g:message
                    code="default.button.edit.label" default="Edit"/></g:link>
            </sec:permitted>

            <sec:ifAllGranted roles="ROLE_ADMIN">
                <g:link class="edit" action="edit" resource="${gameInstance}"><g:message
                        code="default.button.edit.label" default="Edit"/></g:link>
                <g:link class="delete" action="delete" resource="${gameInstance}"
                        onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                    ${message(code: 'default.button.delete.label', default: 'Delete')}
                </g:link>
            </sec:ifAllGranted>
        </fieldset>
</div>
</body>
</html>
