package com.afollestad.recyclical

import androidx.viewbinding.ViewBinding

abstract class BindingViewHolder<VB: ViewBinding>(val binding: VB): ViewHolder(binding.root)