package com.example

import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicInteger

object PersonObject {
    private val idCounter = AtomicInteger()
    private val persons = CopyOnWriteArraySet<Person>() // Person will be our database, because our app is asynchronous we use these primitives, it will copy multiple instances when we need to


    fun add(p: Person): Person { // add function that allows us to insert a person inside our fake database
        if (persons.contains(p)) {
            return persons.find { it == p }!!
        }
        p.id = idCounter.incrementAndGet()
        persons.add(p)
        return p
    }


    fun get(id: String): Person = // get function where id is a string
        persons.find { it.id.toString() == id }
            ?: throw IllegalArgumentException("No entity found for $id")

    fun get(id: Int): Person = get(id.toString()) // get function where id is an int

    fun getAll(): List<Person> = persons.toList() // get function to list all persons

    fun remove(p: Person) { // remove person by the object person
        if (!persons.contains(p)) {
            throw java.lang.IllegalArgumentException("Person not stored in our database")
        }
        persons.remove(p)
    }

    fun remove(id: String): Boolean = persons.remove(get(id)) // remove person by id where id is a string

    fun remove(id: Int): Boolean = persons.remove(get(id)) // remove person by id where id is an int

    fun clear(): Unit = persons.clear() // remove everything from the database
}