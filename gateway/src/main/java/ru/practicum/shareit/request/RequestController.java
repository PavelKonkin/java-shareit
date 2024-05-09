package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constant.Constants;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping(path = "/requests")
@Slf4j
@Validated
public class RequestController {
    private final RequestClient requestClient;

    @Autowired
    public RequestController(RequestClient requestClient) {
        this.requestClient = requestClient;
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> get(@RequestHeader(Constants.USER_HEADER) long userId,
                              @PathVariable long requestId) {
        log.info("Get request with {} by user with id {}", requestId, userId);
        return requestClient.get(userId, requestId);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(Constants.USER_HEADER) long userId,
                                 @Valid @RequestBody ItemRequestCreateDto requestCreateDto) {
        log.info("Create request {} by user with id {}", requestCreateDto, userId);
        return requestClient.create(requestCreateDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllOwn(@RequestHeader(Constants.USER_HEADER) long userId) {
        log.info("Get requests of user with id {}", userId);
        return requestClient.getAllOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader(Constants.USER_HEADER) long userId,
                                       @RequestParam(defaultValue = "0") @Min(0) int from,
                                       @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Get all requests starting from {}," +
                " by {} item per page for user with id {}", from, size, userId);
        return requestClient.getAll(userId, from, size);
    }
}
