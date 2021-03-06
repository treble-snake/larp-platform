package ru.srms.larp.platform

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import ru.srms.larp.platform.game.mail.LetterContent
import ru.srms.larp.platform.game.mail.LetterController
import spock.lang.Specification

@TestFor(LetterController)
@Mock(LetterContent)
class LetterControllerSpec extends Specification {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void "Test the index action returns the correct model"() {

        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            !model.letterInstanceList
            model.letterInstanceCount == 0
    }

    void "Test the create action returns the correct model"() {
        when:"The create action is executed"
            controller.compose()

        then:"The model is correctly created"
            model.letterInstance!= null
    }

    void "Test the save action correctly persists an instance"() {

        when:"The save action is executed with an invalid instance"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'POST'
            def letter = new LetterContent()
            letter.validate()
            controller.save(letter)

        then:"The create view is rendered again with the correct model"
            model.letterInstance!= null
            view == 'create'

        when:"The save action is executed with a valid instance"
            response.reset()
            populateValidParams(params)
            letter = new LetterContent(params)

            controller.save(letter)

        then:"A redirect is issued to the show action"
            response.redirectedUrl == '/letter/show/1'
            controller.flash.message != null
            LetterContent.count() == 1
    }

    void "Test that the show action returns the correct model"() {
        when:"The show action is executed with a null domain"
            controller.show(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the show action"
            populateValidParams(params)
            def letter = new LetterContent(params)
            controller.show(letter)

        then:"A model is populated containing the domain instance"
            model.letterInstance == letter
    }

    void "Test that the edit action returns the correct model"() {
        when:"The edit action is executed with a null domain"
            controller.edit(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the edit action"
            populateValidParams(params)
            def letter = new LetterContent(params)
            controller.edit(letter)

        then:"A model is populated containing the domain instance"
            model.letterInstance == letter
    }

    void "Test the update action performs an update on a valid domain instance"() {
        when:"Update is called for a domain instance that doesn't exist"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'PUT'
            controller.update(null)

        then:"A 404 error is returned"
            response.redirectedUrl == '/letter/index'
            flash.message != null


        when:"An invalid domain instance is passed to the update action"
            response.reset()
            def letter = new LetterContent()
            letter.validate()
            controller.update(letter)

        then:"The edit view is rendered again with the invalid instance"
            view == 'edit'
            model.letterInstance == letter

        when:"A valid domain instance is passed to the update action"
            response.reset()
            populateValidParams(params)
            letter = new LetterContent(params).save(flush: true)
            controller.update(letter)

        then:"A redirect is issues to the show action"
            response.redirectedUrl == "/letter/show/$letter.id"
            flash.message != null
    }

    void "Test that the delete action deletes an instance if it exists"() {
        when:"The delete action is called for a null instance"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'DELETE'
            controller.delete(null)

        then:"A 404 is returned"
            response.redirectedUrl == '/letter/index'
            flash.message != null

        when:"A domain instance is created"
            response.reset()
            populateValidParams(params)
            def letter = new LetterContent(params).save(flush: true)

        then:"It exists"
            LetterContent.count() == 1

        when:"The domain instance is passed to the delete action"
            controller.delete(letter)

        then:"The instance is deleted"
            LetterContent.count() == 0
            response.redirectedUrl == '/letter/index'
            flash.message != null
    }
}
