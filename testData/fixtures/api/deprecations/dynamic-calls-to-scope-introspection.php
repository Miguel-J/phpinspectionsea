<?php

function cases_holder() {
    call_user_func(<warning descr="[EA] Emits a runtime warning (cannot call parse_str() dynamically).">'parse_str'</warning>);
    array_walk([], <warning descr="[EA] Emits a runtime warning (cannot call parse_str() dynamically).">'parse_str'</warning>);

    $callback = 'parse_str';
    <warning descr="[EA] Emits a runtime warning (cannot call parse_str() dynamically).">$callback</warning>();
}