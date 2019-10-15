<?php

    echo true  && <warning descr="[EA] '$x === null' construction should be used instead.">is_null($x)</warning>;
    echo false || <warning descr="[EA] '$x === null' construction should be used instead.">is_null($x)</warning>;

    echo <warning descr="[EA] '$x === null' construction should be used instead.">is_null($x)</warning>;
    echo <warning descr="[EA] '$x === null' construction should be used instead.">true  == is_null($x)</warning>;
    echo <warning descr="[EA] '$x === null' construction should be used instead.">false != is_null($x)</warning>;
    echo <warning descr="[EA] '$x === null' construction should be used instead.">is_null($x) === true</warning>;
    echo <warning descr="[EA] '$x === null' construction should be used instead.">is_null($x) !== false</warning>;

    echo <warning descr="[EA] '$x !== null' construction should be used instead.">!is_null($x)</warning>;
    echo <warning descr="[EA] '$x !== null' construction should be used instead.">false == is_null($x)</warning>;
    echo <warning descr="[EA] '$x !== null' construction should be used instead.">true  != is_null($x)</warning>;
    echo <warning descr="[EA] '$x !== null' construction should be used instead.">is_null($x) === false</warning>;
    echo <warning descr="[EA] '$x !== null' construction should be used instead.">is_null($x) !== true</warning>;

    echo <warning descr="[EA] '($x = null) !== null' construction should be used instead.">is_null($x = null) !== true</warning>;
    echo <warning descr="[EA] '($x ?: null) !== null' construction should be used instead.">!is_null($x ?: null)</warning>;
    echo <warning descr="[EA] '($x ?? null) !== null' construction should be used instead.">!is_null($x ?? null)</warning>;
    echo <warning descr="[EA] '($x ? null : null) !== null' construction should be used instead.">!is_null($x ? null : null)</warning>;

    echo is_null();