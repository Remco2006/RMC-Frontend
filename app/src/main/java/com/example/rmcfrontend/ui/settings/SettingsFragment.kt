package com.example.rmcfrontend.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.rmcfrontend.MainActivity
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.api.models.UpdateUserRequest
import com.example.rmcfrontend.auth.TokenManager
import com.example.rmcfrontend.databinding.FragmentSettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tokenManager = TokenManager(requireContext())

        binding.logoutButton.setOnClickListener {
            (activity as? MainActivity)?.logout()
        }

        binding.deleteButton.setOnClickListener {
            val userId = tokenManager.getUserId()
            if (userId <= 0) {
                Toast.makeText(requireContext(), "Geen gebruiker gevonden.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            confirmDelete(userId)
        }

        binding.saveButton.setOnClickListener {
            val userId = tokenManager.getUserId()
            if (userId <= 0) {
                Toast.makeText(requireContext(), "Geen gebruiker gevonden.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val firstName = binding.firstNameInput.text?.toString()?.trim().orEmpty()
            val lastName = binding.lastNameInput.text?.toString()?.trim().orEmpty()
            val email = binding.emailInput.text?.toString()?.trim().orEmpty()

            if (firstName.isBlank() || lastName.isBlank() || email.isBlank()) {
                Toast.makeText(requireContext(), "Vul alle velden in.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            updateUser(userId, firstName, lastName, email, tokenManager)
        }

        // Load profile once the screen is opened
        val userId = tokenManager.getUserId()
        if (userId > 0) {
            loadUser(userId)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.settingsProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.saveButton.isEnabled = !isLoading
        binding.logoutButton.isEnabled = !isLoading
        binding.deleteButton.isEnabled = !isLoading
        binding.firstNameInput.isEnabled = !isLoading
        binding.lastNameInput.isEnabled = !isLoading
        binding.emailInput.isEnabled = !isLoading
    }

    private fun confirmDelete(userId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(com.example.rmcfrontend.R.string.confirm_delete_title))
            .setMessage(getString(com.example.rmcfrontend.R.string.confirm_delete_message))
            .setNegativeButton(getString(com.example.rmcfrontend.R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(com.example.rmcfrontend.R.string.delete)) { dialog, _ ->
                dialog.dismiss()
                deleteUser(userId)
            }
            .show()
    }

    private fun deleteUser(userId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                setLoading(true)
                val response = withContext(Dispatchers.IO) {
                    ApiClient.usersApi.deleteUser(userId)
                }

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Account verwijderd.", Toast.LENGTH_LONG).show()
                    (activity as? MainActivity)?.logout()
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(requireContext(), "Sessie verlopen. Log opnieuw in.", Toast.LENGTH_LONG).show()
                        (activity as? MainActivity)?.logout()
                        return@launch
                    }
                    Toast.makeText(
                        requireContext(),
                        "Verwijderen mislukt: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Fout: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun loadUser(userId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                setLoading(true)
                val response = withContext(Dispatchers.IO) {
                    ApiClient.usersApi.getUser(userId)
                }

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    binding.firstNameInput.setText(user.firstName)
                    binding.lastNameInput.setText(user.lastName)
                    binding.emailInput.setText(user.email)

                    val created = user.createdAt?.let { "Created: $it" } ?: ""
                    val modified = user.modifiedAt?.let { "Updated: $it" } ?: ""
                    binding.metadataText.text = listOf(created, modified).filter { it.isNotBlank() }.joinToString("\n")
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(requireContext(), "Sessie verlopen. Log opnieuw in.", Toast.LENGTH_LONG).show()
                        (activity as? MainActivity)?.logout()
                        return@launch
                    }
                    Toast.makeText(
                        requireContext(),
                        "Kon gebruikersgegevens niet ophalen: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Fout: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun updateUser(userId: Int, firstName: String, lastName: String, email: String, tokenManager: TokenManager) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                setLoading(true)
                val request = UpdateUserRequest(
                    id = userId.toLong(),
                    firstName = firstName,
                    lastName = lastName,
                    email = email
                )

                val response = withContext(Dispatchers.IO) {
                    ApiClient.usersApi.updateUser(userId, request)
                }

                if (response.isSuccessful) {
                    tokenManager.updateEmail(email)
                    Toast.makeText(requireContext(), "Opgeslagen.", Toast.LENGTH_LONG).show()
                    loadUser(userId) // refresh metadata
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(requireContext(), "Sessie verlopen. Log opnieuw in.", Toast.LENGTH_LONG).show()
                        (activity as? MainActivity)?.logout()
                        return@launch
                    }
                    Toast.makeText(
                        requireContext(),
                        "Opslaan mislukt: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Fout: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
