package com.niraj.dailydiary.presentation.screens.home

import android.annotation.SuppressLint
import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.niraj.dailydiary.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    drawerState: DrawerState,
    onMenuClicked: () -> Unit,
    navigateToWrite : () -> Unit,
    onSignOutClicked: () -> Unit
){
    NavigationDrawer(
        drawerState = drawerState,
        onSignOutClicked = onSignOutClicked
    ) {
        Scaffold(
            topBar = {
                HomeTopBar (
                    onMenuClicked = onMenuClicked
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    navigateToWrite ()
                }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "New Diary")
                }
            }
        ) {

        }
    }
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onSignOutClicked: () -> Unit,
    content : @Composable () -> Unit
){
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                 content = {
                     NavigationDrawerItem(
                         label = {
                             Row( Modifier.padding(horizontal = 12.dp)){
                                 Image(
                                     painter = painterResource(id = R.drawable.google_logo),
                                     contentDescription = null
                                 )
                                 Spacer(Modifier.width(12.dp))
                                 Text(text = "Sign Out")
                             }
                         },
                         selected = false,
                         onClick = {
                             onSignOutClicked ()
                         }
                     )
                 }
            )
        }
    ) {
        content()
    }
}