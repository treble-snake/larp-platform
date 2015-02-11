import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder as SCH
import ru.srms.larp.platform.game.Game
import ru.srms.larp.platform.game.character.GameCharacter
import ru.srms.larp.platform.game.mail.Letter
import ru.srms.larp.platform.sec.SpringRole
import ru.srms.larp.platform.sec.SpringUser
import ru.srms.larp.platform.sec.SpringUserSpringRole

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION

class BootStrap {

    def aclService
    def aclUtilService

    def init = { servletContext ->
        if(Letter.count == 0)
        {
            new Letter(subject: "hi", text: "howdy?").save()
            new Letter(subject: "привет", text: "какчо?").save()

            def roleAdmin = new SpringRole(authority: 'ROLE_ADMIN').save()
            def roleGm = new SpringRole(authority: 'ROLE_GM').save()

            def admin = new SpringUser(username: 'admin', password: 'admin1').save()
            SpringUserSpringRole.create(admin, roleAdmin)

            def gm1 = new SpringUser(username: "gm1", password: "admin1").save()
            SpringUserSpringRole.create(gm1, roleGm)
            def gm2 = new SpringUser(username: "gm2", password: "admin1").save()

            def usr1 = new SpringUser(username: "usr1", password: "admin1").save()
            def usr2 = new SpringUser(username: "usr2", password: "admin1").save()

            Game game1 = new Game(title: "Красная шапочка", alias: "the-red-hat",
                    overview: "Игра по знаменитой сказке.")
                    .addToMasters(gm1)
                    .save()


//            have to be authenticated as an admin to create ACLs
            SCH.context.authentication = new UsernamePasswordAuthenticationToken(
                    'admin', 'admin1', AuthorityUtils.createAuthorityList('ROLE_ADMIN'))

            aclUtilService.addPermission(game1, gm1.username, ADMINISTRATION)

            def redHat = new GameCharacter(name: "красная шапочка", game: game1).save()
            def wolf = new GameCharacter(name: "волк", game: game1).save()
            game1.save()

            usr1.addToCharacters(wolf).save()

            // game 2
            Game game2 = new Game(title: "Камелот", alias: "CamelotAge77",
                    overview: "Игра о славных ряцарях камелота.")
            game2.addToMasters(gm1).addToMasters(gm2).save()

            def king = new GameCharacter(name: "Король Артур", game: game2, player: usr1).save()
            def lancelot = new GameCharacter(name: "Ланселот", game: game2, player: usr2).save()
            def merlin = new GameCharacter(name: "Мерлин", game: game2, player: usr2).save()

            game2.save()
            merlin.player = usr2
            merlin.save()
        }

    }
    def destroy = {
    }
}
