package com.gematriga.nopepad

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.PatternMatcher
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gematriga.nopepad.adapter.NotesAdapter
import com.gematriga.nopepad.database.NotesDatabase
import com.gematriga.nopepad.databinding.FragmentCreateNoteBinding
import com.gematriga.nopepad.entities.Notes
import com.gematriga.nopepad.util.NoteBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.android.synthetic.main.fragment_create_note.slideMore
import kotlinx.android.synthetic.main.fragment_notes_bottom_sheet.*
import kotlinx.android.synthetic.main.item_rv_notes.*
import kotlinx.android.synthetic.main.item_rv_notes.view.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlinx.android.synthetic.main.fragment_notes_bottom_sheet.layoutImage as layoutImage1


@Suppress("DEPRECATION")
class CreateNoteFragment : BaseFragment(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    var selectedColor = "#2e282a"
    var currentDate:String? = null
    private var READ_STORAGE_PERM = 123
    private var REQUEST_CODE_IMAGE = 456
    private var selectedImagePath = ""
    private var webLink = ""
    private var noteId = -1
    private var _binding: FragmentCreateNoteBinding? = null
    private val binding get() = _binding!!
    /*private lateinit var firestore : FirebaseFirestore
    private lateinit var auth: FirebaseAuth*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        noteId = requireArguments().getInt("noteId",-1)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateNoteBinding.inflate(inflater, container, false)
        val view = binding.root


        return view

    }

    companion object{
        @JvmStatic
        fun newInstance()=
            CreateNoteFragment().apply {
                arguments = Bundle().apply {  }
            }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        try{
            if (noteId != -1){
                launch {

                    context?.let {
                        var notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)
                        chooseColor.setBackgroundColor(Color.parseColor(notes.noteColor))
                        binding.titleEditText.setText(notes.title!!)
                        binding.noteEditText.setText(notes.noteText!!)
                        if (notes.imgPath != ""){
                            selectedImagePath = notes.imgPath!!
                            binding.imageNote.setImageBitmap(BitmapFactory.decodeFile(notes.imgPath))
                            binding.layoutImage.visibility = View.VISIBLE
                            binding.imageNote.visibility = View.VISIBLE
                            binding.imageDelete.visibility = View.VISIBLE
                        }else{
                            binding.imageNote.visibility = View.GONE
                            binding.layoutImage.visibility = View.GONE
                            binding.imageDelete.visibility = View.GONE
                        }
                        if (notes.webLink != ""){
                            webLink = notes.webLink!!
                            binding.npAfterWebLink.text = notes.webLink
                            binding.layoutWebUrl.visibility = View.VISIBLE
                            binding.npWebLink.setText(notes.webLink)
                            binding.imageUrlDelete.visibility = View.VISIBLE
                        }else{
                            binding.imageUrlDelete.visibility = View.GONE
                            binding.layoutWebUrl.visibility = View.GONE
                        }

                    }

                }
            }
        }catch (e: Exception){
            Toast.makeText(requireContext(),e.localizedMessage,Toast.LENGTH_LONG).show()
        }


        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            BroadcastReceiver, IntentFilter("bottom_sheet_action")
        )
        //auth = Firebase.auth
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        currentDate = sdf.format(Date())
        chooseColor.setBackgroundColor(Color.parseColor(selectedColor))

        binding.backButtonForCreateFrg.setOnClickListener {
            if(noteId != -1){
                updateNote()
            }else{
                saveNote()
            }


        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if(noteId != -1){
                    updateNote()
                }else{
                    saveNote()
                }
            }
        })

        binding.btnOkay.setOnClickListener {

            if (npWebLink.text.toString().trim().isNotEmpty()){
                checkWebUrl()
            }else{
                Toast.makeText(requireContext(),"Url is Required",Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {

            if (noteId != -1){
                binding.npAfterWebLink.visibility = View.VISIBLE
                binding.layoutWebUrl.visibility = View.GONE
            }else{
                binding.layoutWebUrl.visibility = View.GONE
            }

        }

        binding.imageDelete.setOnClickListener {

            selectedImagePath = ""
            binding.layoutImage.visibility = View.GONE

        }

        binding.imageUrlDelete.setOnClickListener {

            webLink = ""
            binding.npAfterWebLink.visibility = View.GONE
            binding.imageUrlDelete.visibility = View.GONE
            binding.layoutWebUrl.visibility = View.GONE

        }


        binding.npAfterWebLink.setOnClickListener{

            var intent = Intent(Intent.ACTION_VIEW,Uri.parse(binding.npWebLink.text.toString()))
            startActivity(intent)

        }

        slideMore.setOnClickListener {

            var noteBottomSheetFragment = NoteBottomSheetFragment.newInstance(noteId)
            noteBottomSheetFragment.show(requireActivity().supportFragmentManager,"Note Bottom Sheet Fragment")
        }

        chooseColor.setOnClickListener {

                var noteBottomSheetFragment = NoteBottomSheetFragment.newInstance(noteId)
                noteBottomSheetFragment.show(requireActivity().supportFragmentManager,"Note Bottom Sheet Fragment")

        }
    }


    /*private fun saveFirebaseNote(){

        try {
            val title = binding.titleEditText.text.toString()
            val note = binding.noteEditText.text.toString()

            val postMap = hashMapOf<String, Any>()
            postMap.put("email", auth.currentUser!!.email!!)
            postMap.put("title", title)
            postMap.put("note", note)
            postMap.put("date", com.google.firebase.Timestamp.now())

            if (title.isNotEmpty() || note.isNotEmpty()){
                firestore.collection("Notes").add(postMap).addOnSuccessListener {

                    Toast.makeText(activity,"Your note has been saved.", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()

                }.addOnFailureListener {

                    Toast.makeText(activity,it.localizedMessage,Toast.LENGTH_LONG).show()

                }
            }else{
                requireActivity().supportFragmentManager.popBackStack()
            }

        }catch (e: java.lang.Exception){
            Toast.makeText(activity,e.localizedMessage,Toast.LENGTH_LONG).show()
        }

    }*/


    private fun updateNote(){

        launch {

            context?.let{
                val notes= NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)

                notes.title = binding.titleEditText.text.toString()
                notes.noteText = binding.noteEditText.text.toString()
                notes.dateTime = currentDate
                notes.noteColor = selectedColor
                notes.imgPath = selectedImagePath
                notes.webLink = webLink

                NotesDatabase.getDatabase(it).noteDao().updateNote(notes)
                binding.titleEditText.setText("")
                binding.noteEditText.setText("")
                binding.layoutImage.visibility = View.GONE
                binding.imageNote.visibility = View.GONE
                binding.npAfterWebLink.visibility = View.GONE
                replaceFragment(HomeFragment.newInstance(),false)

            }
        }

    }

    private fun saveNote(){

        if(binding.noteEditText.text.isEmpty() && binding.titleEditText.text.isEmpty()){

            requireActivity().supportFragmentManager.popBackStack()

        }else{

            try{
                launch {

                    val notes= Notes()
                    notes.title = binding.titleEditText.text.toString()
                    notes.noteText = binding.noteEditText.text.toString()
                    notes.dateTime = currentDate
                    notes.noteColor = selectedColor
                    notes.imgPath = selectedImagePath
                    notes.webLink = webLink
                    context?.let{
                        NotesDatabase.getDatabase(it).noteDao().insertNotes(notes)
                        binding.titleEditText.setText("")
                        binding.noteEditText.setText("")
                        binding.layoutImage.visibility = View.GONE
                        binding.imageNote.visibility = View.GONE
                        binding.npAfterWebLink.visibility = View.GONE
                        replaceFragment(HomeFragment.newInstance(),false)

                    }



                }
            }catch (e: Exception){
                Toast.makeText(activity,e.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun deleteNote(){

        launch {

            context?.let {

                NotesDatabase.getDatabase(it).noteDao().deleteSpecificNote(noteId)
                requireActivity().supportFragmentManager.popBackStack()
            }

        }

    }

    private fun checkWebUrl(){

        if (Patterns.WEB_URL.matcher(binding.npWebLink.text.toString()).matches()) {
            binding.layoutWebUrl.visibility = View.GONE
            binding.npWebLink.isEnabled = false
            webLink = binding.npWebLink.text.toString()
            binding.npAfterWebLink.visibility = View.VISIBLE
            binding.npAfterWebLink.text = binding.npWebLink.text.toString()
        }else{
            Toast.makeText(requireContext(),"Url is not valid.",Toast.LENGTH_SHORT).show()
        }

    }

    private val BroadcastReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            val actionColor = intent!!.getStringExtra("action")

            when(actionColor!!){

                "Blue" -> {

                    selectedColor = intent.getStringExtra("selectedColor")!!
                    chooseColor.setBackgroundColor(Color.parseColor(selectedColor))

                }

                "Yellow" -> {

                    selectedColor = intent.getStringExtra("selectedColor")!!
                    chooseColor.setBackgroundColor(Color.parseColor(selectedColor))

                }

                "Green" -> {

                    selectedColor = intent.getStringExtra("selectedColor")!!
                    chooseColor.setBackgroundColor(Color.parseColor(selectedColor))

                }

                "Image" -> {

                    readStorageTask()
                    binding.layoutWebUrl.visibility = View.GONE

                }

                "WebUrl" -> {

                    binding.layoutWebUrl.visibility = View.VISIBLE

                }

                "DeleteNote" ->{

                    deleteNote()

                }

                else -> {
                    binding.layoutWebUrl.visibility = View.GONE
                    binding.layoutImage.visibility = View.GONE
                    binding.imageNote.visibility = View.GONE
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    chooseColor.setBackgroundColor(Color.parseColor(selectedColor))

                }
            }

        }

    }

    override fun onDestroy() {

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(BroadcastReceiver)
        super.onDestroy()
    }

    private fun hasReadStoragePerm(): Boolean{
        return EasyPermissions.hasPermissions(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE)
    }


    private fun readStorageTask(){
        if (hasReadStoragePerm()){

            pickImageFromGallery()

        }else{

            EasyPermissions.requestPermissions(
                requireActivity(),
                getString(R.string.storage_permission_text),
                READ_STORAGE_PERM,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

        }
    }

    private fun pickImageFromGallery(){

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(requireActivity().packageManager) != null){
            startActivityForResult(intent,REQUEST_CODE_IMAGE)
        }

    }

    private fun getPathFromUri(contentUri: Uri): String? {

        var filePath: String?
        val cursor = requireActivity().contentResolver.query(contentUri,null,null,null,null)
        if (cursor == null){
            filePath = contentUri.path
        }else{
            cursor.moveToFirst()
            val index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                val selectedImageUrl = data.data
                if (selectedImageUrl != null) {
                    try {
                        val inputStream = requireActivity().contentResolver.openInputStream(selectedImageUrl)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.imageNote.setImageBitmap(bitmap)
                        binding.imageNote.visibility = View.VISIBLE
                        layoutImage.visibility = View.VISIBLE

                        selectedImagePath = getPathFromUri(selectedImageUrl)!!
                    } catch (e: Exception) {
                        println(e.localizedMessage)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,requireActivity())
    }


    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(requireActivity(),perms)){
            AppSettingsDialog.Builder(requireActivity()).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onRationaleDenied(requestCode: Int) {

    }

    override fun onRationaleAccepted(requestCode: Int) {

    }

    fun replaceFragment(fragment: Fragment, istransition: Boolean){

        val fragmentTransition = requireActivity().supportFragmentManager.beginTransaction()

        if (istransition){
            fragmentTransition.setCustomAnimations(android.R.anim.slide_out_right,android.R.anim.slide_in_left)
        }
        fragmentTransition.add(R.id.frame_layout,fragment).addToBackStack(fragment.javaClass.simpleName).commit()
    }

}