package com.berfinilik.moviesappkotlin.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.MovieViewModelFactory
import com.berfinilik.moviesappkotlin.R
import com.berfinilik.moviesappkotlin.adapters.CategoriesAdapter
import com.berfinilik.moviesappkotlin.adapters.PopularMoviesAdapter
import com.berfinilik.moviesappkotlin.adapters.RandomMovieAdapter
import com.berfinilik.moviesappkotlin.adapters.SearchMoviesAdapter
import com.berfinilik.moviesappkotlin.api.ApiClient
import com.berfinilik.moviesappkotlin.data.repository.MovieRepository
import com.berfinilik.moviesappkotlin.databinding.FragmentHomeBinding
import com.berfinilik.moviesappkotlin.viewmodels.MovieViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.berfinilik.moviesappkotlin.data.model.MovieResult
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import kotlin.random.Random


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy { MovieRepository(ApiClient.apiService, requireContext()) }
    private val movieViewModel: MovieViewModel by viewModels {
        MovieViewModelFactory(repository)
    }

    private lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var popularMoviesAdapter: PopularMoviesAdapter
    private lateinit var searchMoviesAdapter: SearchMoviesAdapter




    companion object {
        private const val VOICE_RECOGNITION_REQUEST_CODE = 1
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModel()
        movieViewModel.fetchMovieGenres()
        movieViewModel.fetchPopularMovies()
        fetchFirstNameFromFirestore()
        loadRandomMovies()

        binding.searchViewFilm.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val action = HomeFragmentDirections.actionHomeFragmentToSearchResultsFragment(it)
                    findNavController().navigate(action)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        binding.imageViewFavourite.setOnClickListener {
            val action=HomeFragmentDirections.actionHomeFragmentToFavouritesFragment()
            findNavController().navigate(action)
        }
        binding.microphoneImageView.setOnClickListener {
            checkAudioPermission()
        }
        binding.viewAllPopular.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToAllPopularMoviesFragment()
            findNavController().navigate(action)
        }


    }

    private fun setupRecyclerViews() {
        categoriesAdapter = CategoriesAdapter(emptyList()) { category ->
            val action = HomeFragmentDirections.actionHomeFragmentToCategoryMoviesFragment(
                category.id,
                category.name
            )
            findNavController().navigate(action)
        }
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoriesAdapter
        }

        popularMoviesAdapter = PopularMoviesAdapter(emptyList<MovieResult>()) { selectedMovie: MovieResult ->
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(selectedMovie.id)
            findNavController().navigate(action)
        }
        binding.popularsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 1, GridLayoutManager.HORIZONTAL, false)
            adapter = popularMoviesAdapter


            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    if (!movieViewModel.isLoading && lastVisibleItemPosition + 5 >= totalItemCount) {
                        movieViewModel.fetchPopularMovies()
                    }
                }
            })

        }
    }

    private fun observeViewModel() {
        movieViewModel.genresLiveData.observe(viewLifecycleOwner) { genresResponse ->
            genresResponse?.let {
                if (it.genres.isNotEmpty()) {
                    binding.categoriesRecyclerView.post {
                        categoriesAdapter.updateData(it.genres)
                    }
                } else {
                    showSnackbar("Kategoriler yüklenemedi")
                }
            }
        }

        movieViewModel.popularMoviesLiveData.observe(viewLifecycleOwner) { popularMoviesResponse ->
            popularMoviesResponse?.let {
                if (it.results.isNotEmpty()) {
                    binding.popularsRecyclerView.post {
                        popularMoviesAdapter.updateData(it.results)
                    }
                } else {
                    showSnackbar("Popüler filmler yüklenemedi")
                }
            }
        }

        movieViewModel.searchResultsLiveData.observe(viewLifecycleOwner) { searchResults: List<MovieResult>? ->
            searchResults?.let { results ->
                if (results.isNotEmpty()) {
                    searchMoviesAdapter.updateData(results)
                } else {
                    showSnackbar("Arama sonucu bulunamadı.")
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    private fun fetchFirstNameFromFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (isAdded && _binding != null) {
                        val firstName = document.getString("firstName") ?: "Anonim"
                        binding.textViewUserName.text = String.format(getString(R.string.welcome_message), firstName)
                        val profilePictureUrl = document.getString("profilePicture")
                        if (!profilePictureUrl.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(profilePictureUrl)
                                .circleCrop()
                                .skipMemoryCache(true)
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                .into(binding.imageViewProfile)
                        } else {
                            binding.imageViewProfile.setImageResource(R.drawable.adduserphoto)
                        }
                    }
                }
                .addOnFailureListener {
                    if (isAdded && _binding != null) {
                        binding.textViewUserName.text = String.format(getString(R.string.welcome_message), "Anonim")
                        binding.imageViewProfile.setImageResource(R.drawable.adduserphoto)
                    }
                }
        } else {
            if (isAdded && _binding != null) {
                binding.textViewUserName.text = String.format(getString(R.string.welcome_message), "Anonim")
                binding.imageViewProfile.setImageResource(R.drawable.adduserphoto)
            }
        }
    }




    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_REQUEST_CODE
            )
        } else {
            startVoiceRecognition()
        }
    }
    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Lütfen bir şeyler söyleyin")
        }

        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(binding.root, "Ses tanıma desteklenmiyor.", Snackbar.LENGTH_LONG).show()
        }
    }
    private fun loadRandomMovies() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val randomPage = Random.nextInt(1, 100)
                val response = repository.getPopularMovies(page = randomPage)

                if (response.isSuccessful) {
                    val movies = response.body()?.results?.shuffled()?.take(10) // İlk 10 film
                    if (!movies.isNullOrEmpty()) {
                        setupRandomMoviesViewPager(movies)
                    } else {
                        showSnackbar("Rastgele film bulunamadı.")
                    }
                } else {
                    showSnackbar("Filmler yüklenirken hata oluştu: ${response.message()}")
                }
            } catch (e: Exception) {
                showSnackbar("Filmler yüklenemedi: ${e.message}")
            }
        }
    }



    private fun setupRandomMoviesViewPager(movies: List<MovieResult>) {
        val adapter = RandomMovieAdapter(movies) { movie ->
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(movie.id)
            findNavController().navigate(action)
        }
        binding.randomMovieViewPager.adapter = adapter
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            matches?.firstOrNull()?.let { recognizedText ->
                binding.searchViewFilm.setQuery(recognizedText, true)
                movieViewModel.searchMovies(recognizedText)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        binding.searchViewFilm.setQuery("", false)
        binding.searchViewFilm.clearFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
