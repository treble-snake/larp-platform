<%@ page import="ru.srms.larp.platform.game.Game" %>
<sec:ifLoggedIn>
    <g:set var="template" value="/nested/contentWithActions"/>
</sec:ifLoggedIn>
<sec:ifNotLoggedIn>
    <g:set var="template" value="/nested/contentWithoutActions"/>
</sec:ifNotLoggedIn>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${template}">
    <g:set var="subject" value="${gameInstanceList as List<Game>}"/>
    <g:set var="title" value="Все игры"/>
    <title>${title}</title>
</head>

<body>

<content tag="content">
    <table class="ui celled padded very basic table">
        <thead>
        <tr>
            <g:sortableColumn property="title" title="Название"/>
            <th>Описание</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${subject}" var="game">
            <tr>
                <td>
                    <link:game gameAlias="${game.alias}">${game.title}</link:game>
                </td>
                <td>${game.overview}</td>
            </tr>
        </g:each>
        </tbody>
        <tfoot>
        <tr><th colspan="2">
            <div class="ui right floated pagination menu">
                <g:semanticPaginate total="${gameInstanceCount ?: 0}"/>
            </div>
        </th></tr>
        </tfoot>
    </table>
</content>

<sec:ifLoggedIn>
    <content tag="actions">
        <g:link class="item" action="create"><i
            class="add green icon"></i> Создать игру</g:link>
</content>
</sec:ifLoggedIn>
</body>
</html>
