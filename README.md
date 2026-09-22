# MaceTotemGuard 3.0.0

Paper 1.21.11.

## Exact behavior

A player can successfully pop **at most one Totem per server tick**.

After that first Totem pop, the player is protected from **all further damage for the remainder of that same server tick**.

Example with one Totem:

Tick 100:
- Mace damage -> Totem pops
- More mace/other damage -> cancelled
- Player survives the tick

Tick 101:
- Damage works normally again
- No Totem is available -> lethal damage can kill the player normally

Example with multiple Totems:

Tick 100:
- Totem #1 -> allowed
- Totem #2 -> blocked
- Totem #3 -> blocked

Tick 101:
- Totem #2 can pop if needed

The plugin does not permanently change damage, does not disable mace kills, and does not provide protection beyond the remainder of the tick in which a Totem successfully pops.
