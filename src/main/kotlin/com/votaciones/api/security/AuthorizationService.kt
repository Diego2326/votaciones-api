package com.votaciones.api.security

import com.votaciones.api.common.exception.ForbiddenOperationException
import com.votaciones.api.tournament.domain.TournamentEntity
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val securityUtils: SecurityUtils,
) {

    fun currentUserId() = securityUtils.currentUserId()

    fun isAdmin(): Boolean = securityUtils.hasRole("ADMIN")

    fun assertCanManageTournament(tournament: TournamentEntity) {
        if (isAdmin()) {
            return
        }

        if (tournament.createdBy.id != securityUtils.currentUserId()) {
            throw ForbiddenOperationException("You do not have permission to manage this tournament")
        }
    }

    fun assertCanVote() {
        if (!securityUtils.hasAnyRole("VOTER", "ORGANIZER", "ADMIN")) {
            throw ForbiddenOperationException("The current user does not have permission to vote")
        }
    }
}
