package com.example.a517lablearnandroid

import app.cash.turbine.test
import com.example.a517lablearnandroid.utils.PokedexResponse
import com.example.a517lablearnandroid.utils.PokemonApiService
import com.example.a517lablearnandroid.utils.PokemonEntry
import com.example.a517lablearnandroid.utils.PokemonNetwork
import com.example.a517lablearnandroid.utils.PokemonSpecies
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val apiService: PokemonApiService = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock Singleton PokemonNetwork
        mockkObject(PokemonNetwork)
        coEvery { PokemonNetwork.api } returns apiService
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll() // Clear all mocks
    }

    @Test
    fun `fetchPokemon success updates flow`() = runTest {
        // Given
        val mockEntries = listOf(
            PokemonEntry(1, PokemonSpecies("bulbasaur", "url1"))
        )
        val mockResponse = PokedexResponse(mockEntries)
        coEvery { apiService.getKantoPokedex() } returns mockResponse

        // When
        val viewModel = PokemonViewModel() // init calls fetchPokemon
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.pokemonList.test {
            val result = awaitItem()
            assertEquals(mockEntries, result)
        }
    }

    @Test
    fun `fetchPokemon failure handles error`() = runTest {
        // Given
        coEvery { apiService.getKantoPokedex() } throws Exception("Network Error")

        // When
        val viewModel = PokemonViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.pokemonList.test {
            val result = awaitItem()
            assertEquals(emptyList<PokemonEntry>(), result)
        }
    }
}
