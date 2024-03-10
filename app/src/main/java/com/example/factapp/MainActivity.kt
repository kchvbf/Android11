package com.example.factapp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager

import com.google.gson.Gson

class MainActivity : ComponentActivity() {

    private lateinit var navController: NavHostController

    private val viewModel: FactViewModel by viewModels()

    private lateinit var observer: Observer<WorkInfo>
    private lateinit var workInfo: LiveData<WorkInfo>

    private val request = OneTimeWorkRequest.Builder(FactWorker::class.java).build()

    private lateinit var receiver: BroadcastReceiver




    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            Surface(
                color = Color(0xFF794141)
            ) {
                navController = rememberNavController()
                HostScreen(navController, viewModel, request)
            }


        }

        receiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {

                val facts = Gson().fromJson(
                    intent?.getStringExtra("Facts"),
                    Array<Fact>::class.java
                ).toList()

                viewModel.updateFacts(facts)

                navController.navigate("facts")
            }
        }

        registerReceiver(receiver, IntentFilter("com.example.factapp"), Context.RECEIVER_EXPORTED)



        observer = Observer { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                val facts = Gson().fromJson(
                    workInfo.outputData.getString("Facts"),
                    Array<Fact>::class.java
                ).toList()
                viewModel.updateFacts(facts)
                navController.navigate("facts")
            }
        }

        val workManager = WorkManager.getInstance(this)
        workInfo = workManager.getWorkInfoByIdLiveData(request.id)
        workInfo.observe(this, observer)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        workInfo.removeObserver(observer)
    }
}


@Composable
fun SelectionScreen(viewModel: FactViewModel, request: OneTimeWorkRequest) {

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.selection_screen_text),
            fontSize = 25.sp,
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
        ){

            Button(
                onClick = { viewModel.startFactService(context) },
                colors = ButtonDefaults.buttonColors(Color(0xFF008B8B)),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .height(150.dp)
                    .width(150.dp)
            ) {
                Text(text = stringResource(R.string.fact_service_btn_text))
            }

            Spacer(modifier = Modifier.width(20.dp))

            Button(
                onClick = { viewModel.startFactWorkManager(context, request) },
                colors = ButtonDefaults.buttonColors(Color(0xFF008B8B)),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .height(150.dp)
                    .width(150.dp)
            ) {

                Text(text = stringResource(R.string.fact_worker_btn_text))
            }
        }

    }
}


@Composable
fun FactsScreen(viewModel: FactViewModel) {

    val facts by viewModel.facts.observeAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text(
            text = stringResource(R.string.fact_text),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {

            itemsIndexed(facts) { _, Fact ->
                Card(
                    colors = CardDefaults.cardColors(Color(0xFFFFFFE0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                ) {
                    Text(
                        text = Fact.fact,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(20.dp),

                        )

                    Divider(modifier = Modifier.padding(vertical = 10.dp),thickness = 2.dp)

                }
            }
        }
    }
}

@Composable
fun HostScreen(navController: NavHostController, viewModel: FactViewModel, request: OneTimeWorkRequest) {

    NavHost(navController = navController, startDestination = "selection") {

        composable("selection") {
            SelectionScreen(viewModel = viewModel, request)
        }

        composable("facts") {
            FactsScreen(viewModel = viewModel)
        }
    }
}