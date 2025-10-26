package ru.practicum.ewm.event.model;

/** Possible state transition actions for events. */
public enum StateAction {
    SEND_TO_REVIEW,
    CANCEL_REVIEW,
    PUBLISH_EVENT,
    REJECT_EVENT
}