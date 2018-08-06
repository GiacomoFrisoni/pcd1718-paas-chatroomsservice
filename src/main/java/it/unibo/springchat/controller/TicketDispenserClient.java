package it.unibo.springchat.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("pcd1718-paas-ticketdispenserservice")
interface TicketDispenserClient {

    @RequestMapping(method = RequestMethod.GET, value = "/app/ticket/{roomId}")
    Long getTicket(@PathVariable("roomId") final String roomId);
    
}