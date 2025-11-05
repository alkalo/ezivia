package com.ezivia.launcher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ezivia.communication.contacts.FavoriteContact
import com.ezivia.launcher.databinding.ItemFavoriteContactBinding

class FavoriteContactsAdapter(
    private val onCallClick: (FavoriteContact) -> Unit,
    private val onMessageClick: (FavoriteContact) -> Unit,
    private val onVideoCallClick: (FavoriteContact) -> Unit,
) : ListAdapter<FavoriteContact, FavoriteContactsAdapter.ContactViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFavoriteContactBinding.inflate(inflater, parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(
        private val binding: ItemFavoriteContactBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: FavoriteContact) {
            binding.contactName.text = contact.displayName
            binding.contactNumber.text = contact.phoneNumber

            binding.callButton.setOnClickListener { onCallClick(contact) }
            binding.messageButton.setOnClickListener { onMessageClick(contact) }
            binding.videoButton.setOnClickListener { onVideoCallClick(contact) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<FavoriteContact>() {
        override fun areItemsTheSame(oldItem: FavoriteContact, newItem: FavoriteContact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FavoriteContact, newItem: FavoriteContact): Boolean {
            return oldItem == newItem
        }
    }
}
