package com.selodev.tennis.controller

import com.selodev.tennis.service.Config
import com.selodev.tennis.service.Rotation
import com.selodev.tennis.service.TennisService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.servlet.ModelAndView

@Controller
class UserController(
    private val tennisService: TennisService
) {
    @GetMapping("/")
    fun index(): ModelAndView = ModelAndView("rotations")

    @PostMapping("/rotations")
    fun rotations(@RequestBody request: RotationsRequest): ResponseEntity<RotationsResponse> =
        ResponseEntity(RotationsResponse(tennisService.generateRotations(request.players, request.config)), HttpStatus.OK)
}

data class RotationsRequest(
    val config: Config,
    val players: List<String>
)

data class RotationsResponse(
    val rotations: List<Rotation>
)
