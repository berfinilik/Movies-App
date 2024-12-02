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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.berfinilik.moviesappkotlin.MovieViewModelFactory
import com.berfinilik.moviesappkotlin.adapters.CategoriesAdapter
import com.berfinilik.moviesappkotlin.adapters.PopularMoviesAdapter
import com.berfinilik.moviesappkotlin.adapters.SearchMoviesAdapter
import com.berfinilik.moviesappkotlin.api.ApiClient
import com.berfinilik.moviesappkotlin.data.repository.MovieRepository
import com.berfinilik.moviesappkotlin.databinding.FragmentHomeBinding
import com.berfinilik.moviesappkotlin.viewmodels.MovieViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

    private lateinit var adView:AdView

    companion object {
        private const val VOICE_RECOGNITION_REQUEST_CODE = 1
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        context?.let { MobileAds.initialize(it) }
        adView = binding.adView
        loadBannerAd()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModel()
        movieViewModel.fetchMovieGenres()
        movieViewModel.fetchPopularMovies()
        fetchUserNameFromFirestore()
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
    }
    private fun loadBannerAd() {
        val adRequest = AdRequest.Builder()
            .setContentUrl("https://www.imdb.com/")
            .build()
        adView.loadAd(adRequest)
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

        popularMoviesAdapter = PopularMoviesAdapter(emptyList()) { selectedMovie ->
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

        movieViewModel.searchResultsLiveData.observe(viewLifecycleOwner) { searchResults ->
            searchResults?.let {
                if (it.isNotEmpty()) {
                    searchMoviesAdapter.updateData(it)
                } else {
                    showSnackbar("Arama sonucu bulunamadı.")
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    private fun fetchUserNameFromFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userName = document.getString("userName") ?: "Anonim Kullanıcı"
                        binding.textViewUserName.text = "Hoşgeldin $userName"
                    } else {
                        binding.textViewUserName.text = "Hoşgeldin Anonim Kullanıcı"
                    }
                }
                .addOnFailureListener { exception ->
                    showSnackbar("Kullanıcı adı alınamadı: ${exception.message}")
                }
        } else {
            binding.textViewUserName.text = "Hoşgeldin Anonim Kullanıcı"
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
