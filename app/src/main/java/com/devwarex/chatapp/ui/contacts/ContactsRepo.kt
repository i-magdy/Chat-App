package com.devwarex.chatapp.ui.contacts

import android.util.Log
import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.db.Contact
import com.devwarex.chatapp.repos.SearchUserRepo
import com.devwarex.chatapp.utility.PhoneUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Queue
import javax.inject.Inject

class ContactsRepo @Inject constructor(
    private val db: AppDao,
    private val searchRepo: SearchUserRepo
) {

    private val job = CoroutineScope(Dispatchers.IO)

    fun getContacts(): Flow<List<Contact>>  = db.getContacts()

    init {
        job.launch {
            searchRepo.isFound.receiveAsFlow().collectLatest {
                Log.e("user_found","$it")
            }
        }
        searchRepo.findUserByPhone("01016748500")
    }

    fun updateContacts(contacts: Queue<Contact>){
        job.launch {
            while (contacts.isNotEmpty()){
                val contact = contacts.poll() ?: continue
                if (contact.phone.isNotBlank()){
                    db.insertContact(
                        Contact(
                            name = contact.name,
                            phone = PhoneUtil.filterPhoneNumber(contact.phone),
                            isFound = false
                        )
                    )
                }
            }
        }
    }


    fun cancelJobs(){
        job.cancel()
    }
}